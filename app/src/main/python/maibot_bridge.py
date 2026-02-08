"""
MaiBot桥接层 - 完整集成MaiBot项目
"""
import json
import os
import sys
from typing import List, Dict, Any

# 添加MaiBot源码路径
maibot_path = os.path.join(os.path.dirname(__file__), 'maibot')
if maibot_path not in sys.path:
    sys.path.insert(0, maibot_path)


class MaiBotBridge:
    """MaiBot完整功能桥接"""
    
    def __init__(self):
        self.bot_instances = []
        self.initialized = False
        
    def initialize(self, api_key: str, bot_count: int, config: Dict[str, Any] = None):
        """初始化MaiBot实例
        
        Args:
            api_key: DeepSeek API Key
            bot_count: Bot实例数量
            config: 额外配置（可选）
        """
        try:
            # 设置环境变量
            os.environ['DEEPSEEK_API_KEY'] = api_key
            
            # 导入MaiBot核心模块
            from chat.brain_chat.brain import Brain
            from config.config import global_config
            
            # 配置API
            global_config.llm.api_key = api_key
            global_config.llm.model_name = config.get('model_name', 'deepseek-chat') if config else 'deepseek-chat'
            
            # 预定义的Bot配置
            bot_configs = [
                {
                    "name": "麦麦",
                    "personality": "活泼可爱、充满个性",
                    "color": "#FF6B9D",
                    "prompt_style": "轻松活泼，喜欢用表情和语气词"
                },
                {
                    "name": "小智",
                    "personality": "理性严谨的技术专家",
                    "color": "#4A90E2",
                    "prompt_style": "逻辑清晰，注重细节"
                },
                {
                    "name": "诗诗",
                    "personality": "温柔文艺的诗人",
                    "color": "#9B59B6",
                    "prompt_style": "优雅含蓄，富有诗意"
                },
                {
                    "name": "阿乐",
                    "personality": "幽默风趣的段子手",
                    "color": "#F39C12",
                    "prompt_style": "轻松搞笑，善于活跃气氛"
                },
                {
                    "name": "小月",
                    "personality": "温暖贴心的倾听者",
                    "color": "#E91E63",
                    "prompt_style": "温柔体贴，善于理解"
                }
            ]
            
            # 创建Bot实例
            self.bot_instances = []
            for i in range(min(bot_count, len(bot_configs))):
                config_item = bot_configs[i]
                
                # 创建Brain实例（MaiBot的核心）
                bot_brain = Brain(
                    bot_name=config_item["name"],
                    personality=config_item["personality"]
                )
                
                self.bot_instances.append({
                    "id": f"bot_{i}",
                    "name": config_item["name"],
                    "color": config_item["color"],
                    "brain": bot_brain,
                    "config": config_item
                })
            
            self.initialized = True
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
                "id": bot["id"],
                "name": bot["name"],
                "color": bot["color"]
            })
        
        return json.dumps(bot_list, ensure_ascii=False)
    
    async def send_message_async(self, user_message: str, user_name: str = "用户") -> List[Dict[str, Any]]:
        """异步发送消息到所有Bot"""
        if not self.initialized:
            return []
        
        import asyncio
        
        responses = []
        tasks = []
        
        # 为每个Bot创建任务
        for bot in self.bot_instances:
            task = self._process_bot_message(bot, user_message, user_name)
            tasks.append(task)
        
        # 并发执行
        results = await asyncio.gather(*tasks, return_exceptions=True)
        
        # 整理结果
        for bot, result in zip(self.bot_instances, results):
            if isinstance(result, Exception):
                content = f"出错了: {str(result)}"
            else:
                content = result
            
            responses.append({
                "bot_id": bot["id"],
                "bot_name": bot["name"],
                "color": bot["color"],
                "content": content
            })
        
        return responses
    
    async def _process_bot_message(self, bot: Dict, user_message: str, user_name: str) -> str:
        """处理单个Bot的消息"""
        try:
            brain = bot["brain"]
            
            # 构建消息数据（符合MaiBot格式）
            message_data = {
                "message_info": {
                    "platform": "android",
                    "message_id": f"msg_{id(user_message)}",
                    "time": __import__('time').time(),
                    "group_info": {
                        "group_id": "android_group",
                        "group_name": "Android群聊",
                        "platform": "android"
                    },
                    "user_info": {
                        "user_id": "android_user",
                        "user_nickname": user_name,
                        "user_cardname": user_name,
                        "platform": "android"
                    },
                    "additional_config": {
                        "at_bot": True
                    }
                },
                "raw_message": user_message,
                "processed_plain_text": user_message
            }
            
            # 调用MaiBot的Brain处理
            response = await brain.process_message(message_data)
            
            return response if response else "..."
            
        except Exception as e:
            return f"处理失败: {str(e)}"
    
    def send_message_sync(self, user_message: str, user_name: str = "用户") -> str:
        """同步发送消息（供Java调用）"""
        import asyncio
        
        # 创建事件循环
        loop = asyncio.new_event_loop()
        asyncio.set_event_loop(loop)
        
        try:
            responses = loop.run_until_complete(
                self.send_message_async(user_message, user_name)
            )
            return json.dumps(responses, ensure_ascii=False)
        finally:
            loop.close()
    
    def clear_history(self):
        """清空所有Bot的历史"""
        if not self.initialized:
            return
        
        for bot in self.bot_instances:
            try:
                brain = bot["brain"]
                if hasattr(brain, 'clear_memory'):
                    brain.clear_memory()
            except Exception as e:
                print(f"清空{bot['name']}历史失败: {e}")


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


def send_message(user_message: str, user_name: str = "用户") -> str:
    """发送消息"""
    return _bridge.send_message_sync(user_message, user_name)


def clear_history():
    """清空历史"""
    _bridge.clear_history()
