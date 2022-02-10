package github.nullnet.wifip2p.ChatCode;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import java.util.ArrayList;

import github.nullnet.wifip2p.R;


public class ChatAppMsgAdapter  extends RecyclerView.Adapter<ChatAppMsgViewHolder> {

    private List<ChatAppMsgDTO> msgDtoList = null;
    Context context;
    public ChatAppMsgAdapter(List<ChatAppMsgDTO> msgDtoList,Context context) {
        this.context = context;
        this.msgDtoList = msgDtoList;
    }

    @Override
    public void onBindViewHolder(ChatAppMsgViewHolder holder, int position, @NonNull List<Object> payloads) {
        ChatAppMsgDTO msgDto = this.msgDtoList.get(position);

        if(msgDto.IsImage){
            holder.itemImage.setVisibility(View.VISIBLE);
            Glide.with(context).load(msgDto.getMsgContent()).into(holder.itemImage);
            holder.rightMsgLayout.setVisibility(LinearLayout.GONE);
            holder.leftMsgLayout.setVisibility(LinearLayout.GONE);
        }
        else{
            // If the message is a received message.
            if(msgDto.MSG_TYPE_RECEIVED.equals(msgDto.getMsgType()))
            {
                // Show received message in left linearlayout.
                holder.leftMsgLayout.setVisibility(LinearLayout.VISIBLE);
                holder.leftMsgTextView.setText(msgDto.getMsgContent());
                // Remove left linearlayout.The value should be GONE, can not be INVISIBLE
                // Otherwise each iteview's distance is too big.
                holder.rightMsgLayout.setVisibility(LinearLayout.GONE);

//            holder.devicenameLeft.setText(msgDto.getDeviceName());
                holder.devicenameLeft.setText("Friend");



            }
            // If the message is a sent message.
            else if(msgDto.MSG_TYPE_SENT.equals(msgDto.getMsgType()))
            {
                // Show sent message in right linearlayout.
                holder.rightMsgLayout.setVisibility(LinearLayout.VISIBLE);
                holder.rightMsgTextView.setText(msgDto.getMsgContent());
                // Remove left linearlayout.The value should be GONE, can not be INVISIBLE
                // Otherwise each iteview's distance is too big.
                holder.leftMsgLayout.setVisibility(LinearLayout.GONE);
                holder.devicenamYou.setText("You!");
            }
        }

    }

    @Override
    public ChatAppMsgViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.chat_app_item_view, parent, false);
        return new ChatAppMsgViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAppMsgViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        if(msgDtoList==null)
        {
            msgDtoList = new ArrayList<ChatAppMsgDTO>();
        }
        return msgDtoList.size();
    }
}

