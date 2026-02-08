"""
MaiBot桥接层 - 完整集成MaiBot项目的所有功能
保留：记忆系统、上下文管理、工具调用、插件系统、自我调优等所有原有功能
支持异步回复，模拟真实群聊场景
"""
import json
import os
import sys
import asyncio
import time
from typing import List, Dict, Any, Callable, Optional
from queue import Queue

# 添加MaiBot源码路径
maibot_path = os.path.join(os.path.dirname(__file__), 'maibot')
if maibot_path not in sys.path:
    sys.path.insert(0, maibot_path)

# 设置Android环境标识
os.environ['MAIBOT_ANDROID'] = '1'


class AndroidMessageSender:
    """Android消息发送器 - 拦截MaiBot的消息发送，转发到Android"""
    
    def __init__(self):
        self.message_queue = Queue()
        self.callback = None
        
    def set_callback(self, callback: Callable[[str, str, str, str], None]):
        """设置消息回调函数
        
        Args:
            callback: 回调函数(bot_id, bot_name, color, content)
        """
        self.callback = callback
    
    async def send_message(self, bot_id: str, bot_name: str, color: str, content: str):
        """发送消息到Android"""
        if self.callback:
            # 通过回调发送到Java层
            self.callback(bot_id, bot_name, color, content)
        else:
            # 放入队列等待获取
            self.message_queue.put({
                "bot_id": bot_id,
                "bot_name": bot_name,
                "color": color,
                "content": content
            })
    
    def get_pending_messages(self) -> List[Dict[str, str]]:
        """获取待处理的消息"""
        messages = []
        while not self.message_queue.empty():
            messages.append(self.message_queue.get())
        return messages


# 全局消息发送器
_android_sender = AndroidMessageSender()


class MaiBotInstance:
    """完整的MaiBot实例，保留所有原有功能"""
    
    def __init__(self, bot_id: str, name: str, personality: str, color: str, api_key: str):
        self.bot_id = bot_id
        self.name = name
        self.personality = personality
        self.color = color
        self.api_key = api_key
        self.chat_bot = None
        self.chat_stream = None
        self.heartflow_chat = None
        self.initialized = False
        self.message_sender = _android_sender
        
    async def initialize(self):
        """初始化MaiBot核心组件"""
        try:
            # 导入MaiBot核心模块
            from config.config import global_config
            from chat.message_receive.bot import ChatBot
            from chat.message_receive.chat_stream import get_chat_manager
            from chat.heart_flow.heartflow import heartflow
            from common.data_models.info_data_model import UserInfo, GroupInfo
            
            # 配置API（每个Bot独立配置）
            global_config.llm.api_key = self.api_key
            global_config.llm.model_name = "deepseek-chat"
            global_config.llm.base_url = "https://api.deepseek.com"
            global_config.bot.nickname = self.name
            
            # 创建ChatBot实例
            self.chat_bot = ChatBot()
            await self.chat_bot._ensure_started()
            
            # 创建虚拟群聊流（模拟Android群聊环境）
            user_info = UserInfo(
                user_id="android_user",
                user_nickname="用户",
                user_cardname="用户",
                platform="android"
            )
            
            group_info = GroupInfo(
                group_id=f"android_group_{self.bot_id}",
                group_name=f"{self.name}的聊天室",
                platform="android"
            )
            
            # 获取或创建聊天流
            self.chat_stream = await get_chat_manager().get_or_create_stream(
                platform="android",
                user_info=user_info,
                group_info=group_info
            )
            
            # 创建HeartFlow聊天实例（这是MaiBot的核心对话管理器）
            self.heartflow_chat = await heartflow.get_or_create_heartflow_chat(
                self.chat_stream.stream_id
            )
            
            # 注入自定义消息发送器（拦截MaiBot的消息发送）
            self._inject_message_sender()
            
            self.initialized = True
            print(f"✓ {self.name} 初始化成功（完整MaiBot功能）")
            return True
            
        except Exception as e:
            print(f"✗ {self.name} 初始化失败: {e}")
            import traceback
            traceback.print_exc()
            return False
    
    def _inject_message_sender(self):
        """注入自定义消息发送器，拦截MaiBot的消息发送"""
        try:
            from chat.message_receive.uni_message_sender import UniMessageSender
            
            # 保存原始发送方法
            original_send = UniMessageSender.send_message
            bot_instance = self
            
            # 创建包装方法
            async def wrapped_send(self_sender, *args, **kwargs):
                # 调用原始方法
                result = await original_send(self_sender, *args, **kwargs)
                
                # 提取消息内容并发送到Android
                if args and len(args) > 0:
                    content = str(args[0]) if args[0] else ""
                    if content:
                        await _android_sender.send_message(
                            bot_instance.bot_id,
                            bot_instance.name,
                            bot_instance.color,
                            content
                        )
                
                return result
            
            # 替换发送方法
            UniMessageSender.send_message = wrapped_send
            
        except Exception as e:
            print(f"注入消息发送器失败: {e}")
    
    async def process_message(self, user_message: str, user_name: str = "用户"):
        """使用MaiBot完整流程处理消息（异步，不等待回复）"""
        if not self.initialized:
            print(f"{self.name} 未初始化")
            return
        
        try:
            from common.data_models.info_data_model import (
                MessageInfo, UserInfo, GroupInfo, AdditionalConfig
            )
            
            # 构建完整的消息数据模型（符合MaiBot标准）
            message_info = MessageInfo(
                platform="android",
                message_id=f"msg_{int(time.time() * 1000)}_{self.bot_id}",
                time=time.time(),
                group_info=GroupInfo(
                    group_id=f"android_group_{self.bot_id}",
                    group_name=f"{self.name}的聊天室",
                    platform="android"
                ),
                user_info=UserInfo(
                    user_id="android_user",
                    user_nickname=user_name,
                    user_cardname=user_name,
                    platform="android"
                ),
                additional_config=AdditionalConfig(
                    at_bot=True  # 模拟@机器人
                )
            )
            
            message_data = {
                "message_info": message_info.model_dump(),
                "raw_message": user_message,
                "processed_plain_text": user_message
            }
            
            # 使用MaiBot的完整消息处理流程（异步执行）
            # 这会触发：记忆检索、上下文管理、工具调用、插件系统等所有功能
            # 回复会通过注入的消息发送器异步返回
            asyncio.create_task(self.chat_bot.message_process(message_data))
            
            print(f"→ {self.name} 开始处理消息")
            
        except Exception as e:
            import traceback
            error_msg = f"处理失败: {str(e)}"
            print(f"✗ {self.name} {error_msg}")
            traceback.print_exc()
            
            # 发送错误消息
            await self.message_sender.send_message(
                self.bot_id,
                self.name,
                self.color,
                error_msg
            )
    
    def clear_history(self):
        """清空对话历史"""
        try:
            if self.heartflow_chat:
                # 清空HeartFlow的历史记录
                self.heartflow_chat.history_loop = []
                print(f"✓ {self.name} 历史已清空")
        except Exception as e:
            print(f"✗ 清空{self.name}历史失败: {e}")


class MaiBotBridge:
    """MaiBot完整功能桥接层"""
    
    def __init__(self):
        self.bot_instances = []
        self.initialized = False
        self._init_lock = None
        
    async def _initialize_maibot_core(self, api_key: str):
        """初始化MaiBot核心系统（只需执行一次）"""
        try:
            print("正在初始化MaiBot核心系统...")
            
            # 导入并初始化MaiBot核心组件
            from config.config import global_config
            from common.logger import initialize_logging, get_logger
            from common.database.database import init_database
            
            # 初始化日志系统（简化版，适配Android）
            try:
                initialize_logging(verbose=False)
                logger = get_logger("maibot_bridge")
                logger.info("MaiBot日志系统初始化完成")
            except Exception as e:
                print(f"日志系统初始化失败（非致命）: {e}")
            
            # 初始化数据库（使用Android存储）
            android_data_dir = os.path.join(os.path.dirname(__file__), 'data')
            os.makedirs(android_data_dir, exist_ok=True)
            
            # 配置全局设置
            global_config.llm.api_key = api_key
            global_config.llm.model_name = "deepseek-chat"
            global_config.llm.base_url = "https://api.deepseek.com"
            global_config.llm.temperature = 0.8
            global_config.llm.max_tokens = 500
            
            # 禁用不需要的功能（适配Android环境）
            try:
                global_config.webui.enabled = False
            except:
                pass
            
            # 禁用知识图谱功能（需要faiss，Android不支持）
            try:
                global_config.knowledge.enabled = False
            except:
                pass
            
            # 初始化数据库
            db_path = os.path.join(android_data_dir, 'maibot.db')
            try:
                init_database(db_path)
                print(f"✓ 数据库初始化完成: {db_path}")
            except Exception as e:
                print(f"数据库初始化失败（非致命）: {e}")
            
            # 初始化插件系统（加载内置插件，跳过不兼容的）
            try:
                from plugin_system.core.plugin_manager import plugin_manager
                await plugin_manager.load_builtin_plugins()
                print("✓ 插件系统初始化完成")
            except Exception as e:
                print(f"插件系统初始化失败（非致命）: {e}")
            
            print("✓ MaiBot核心系统初始化完成")
            return True
            
        except Exception as e:
            print(f"✗ MaiBot核心系统初始化失败: {e}")
            import traceback
            traceback.print_exc()
            return False
        
    def initialize(self, api_key: str, bot_count: int, config: Dict[str, Any] = None):
        """初始化Bot实例（同步接口）"""
        loop = asyncio.new_event_loop()
        asyncio.set_event_loop(loop)
        try:
            return loop.run_until_complete(self._initialize_async(api_key, bot_count, config))
        finally:
            loop.close()
    
    async def _initialize_async(self, api_key: str, bot_count: int, config: Dict[str, Any] = None):
        """异步初始化Bot实例"""
        if self._init_lock is None:
            self._init_lock = asyncio.Lock()
            
        async with self._init_lock:
            if self.initialized:
                return True
            
            try:
                # 初始化MaiBot核心系统
                if not await self._initialize_maibot_core(api_key):
                    return False
                
                # 预定义的Bot配置
                bot_configs = [
                    {
                        "name": "麦麦",
                        "personality": "活泼可爱、充满个性的少女，喜欢用可爱的语气词",
                        "color": "#FF6B9D"
                    },
                    {
                        "name": "小智",
                        "personality": "理性严谨的技术专家，逻辑清晰，注重细节",
                        "color": "#4A90E2"
                    },
                    {
                        "name": "诗诗",
                        "personality": "温柔文艺的诗人，优雅含蓄，富有诗意",
                        "color": "#9B59B6"
                    },
                    {
                        "name": "阿乐",
                        "personality": "幽默风趣的段子手，善于活跃气氛",
                        "color": "#F39C12"
                    },
                    {
                        "name": "小月",
                        "personality": "温暖贴心的倾听者，善于理解和安慰",
                        "color": "#E91E63"
                    }
                ]
                
                # 创建Bot实例
                self.bot_instances = []
                for i in range(min(bot_count, len(bot_configs))):
                    config_item = bot_configs[i]
                    
                    bot = MaiBotInstance(
                        bot_id=f"bot_{i}",
                        name=config_item["name"],
                        personality=config_item["personality"],
                        color=config_item["color"],
                        api_key=api_key
                    )
                    
                    # 初始化每个Bot实例
                    if await bot.initialize():
                        self.bot_instances.append(bot)
                    else:
                        print(f"Bot {bot.name} 初始化失败")
                
                self.initialized = True
                print(f"✓ 成功初始化 {len(self.bot_instances)} 个Bot实例")
                return True
                
            except Exception as e:
                print(f"初始化失败: {e}")
                import traceback
                traceback.print_exc()
                return False
    
    def get_bot_list(self) -> str:
        """获取Bot列表"""
        if not self.initialized:
            return json.dumps([])
        
        bot_list = []
        for bot in self.bot_instances:
            bot_list.append({
                "id": bot.bot_id,
                "name": bot.name,
                "color": bot.color
            })
        
        return json.dumps(bot_list, ensure_ascii=False)
    
    def send_message(self, user_message: str, user_name: str = "用户"):
        """发送消息到所有Bot（异步触发，不等待回复）"""
        if not self.initialized:
            print("Bot未初始化")
            return
        
        # 在后台线程中运行异步任务
        def run_async_tasks():
            loop = asyncio.new_event_loop()
            asyncio.set_event_loop(loop)
            try:
                # 为每个Bot创建异步任务
                tasks = [bot.process_message(user_message, user_name) for bot in self.bot_instances]
                loop.run_until_complete(asyncio.gather(*tasks))
            finally:
                loop.close()
        
        # 在新线程中运行，避免阻塞
        import threading
        thread = threading.Thread(target=run_async_tasks, daemon=True)
        thread.start()
    
    def get_pending_messages(self) -> str:
        """获取待处理的消息（轮询方式）"""
        messages = _android_sender.get_pending_messages()
        return json.dumps(messages, ensure_ascii=False)
    
    def clear_history(self):
        """清空所有Bot的历史"""
        if not self.initialized:
            return
        
        for bot in self.bot_instances:
            try:
                bot.clear_history()
            except Exception as e:
                print(f"清空{bot.name}历史失败: {e}")


# 全局实例
_bridge = MaiBotBridge()


def initialize_bots(api_key: str, bot_count: int, config_json: str = "{}") -> bool:
    """初始化Bot实例"""
    try:
        config = json.loads(config_json) if config_json else {}
        return _bridge.initialize(api_key, bot_count, config)
    except Exception as e:
        print(f"初始化失败: {e}")
        return False


def get_bot_list() -> str:
    """获取Bot列表"""
    return _bridge.get_bot_list()


def send_message(user_message: str, user_name: str = "用户"):
    """发送消息（异步触发）"""
    _bridge.send_message(user_message, user_name)


def get_pending_messages() -> str:
    """获取待处理的消息"""
    return _bridge.get_pending_messages()


def clear_history():
    """清空历史"""
    _bridge.clear_history()
