package com.maibot.multichat;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
    private List<ChatMessage> messages;
    private SimpleDateFormat timeFormat;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout messageContainer;
        private TextView senderName;
        private TextView messageContent;
        private TextView messageTime;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageContainer = itemView.findViewById(R.id.messageContainer);
            senderName = itemView.findViewById(R.id.senderName);
            messageContent = itemView.findViewById(R.id.messageContent);
            messageTime = itemView.findViewById(R.id.messageTime);
        }

        public void bind(ChatMessage message) {
            senderName.setText(message.getSenderName());
            messageContent.setText(message.getContent());
            messageTime.setText(timeFormat.format(new Date(message.getTimestamp())));

            try {
                senderName.setTextColor(Color.parseColor(message.getColor()));
            } catch (Exception e) {
                senderName.setTextColor(Color.BLACK);
            }

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) messageContainer.getLayoutParams();
            if (message.isUser()) {
                params.gravity = Gravity.END;
                messageContainer.setBackgroundResource(R.drawable.bg_message_user);
            } else {
                params.gravity = Gravity.START;
                messageContainer.setBackgroundResource(R.drawable.bg_message_bot);
            }
            messageContainer.setLayoutParams(params);
        }
    }
}
