package github.nullnet.wifip2p.ChatCode;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import github.nullnet.wifip2p.R;

public class ChatAppMsgViewHolder extends RecyclerView.ViewHolder{
    LinearLayout leftMsgLayout;
    LinearLayout rightMsgLayout;
    TextView leftMsgTextView;
    TextView rightMsgTextView;
    TextView devicenameLeft;
    TextView devicenamYou;
    ImageView itemImage;
    public ChatAppMsgViewHolder(View itemView) {
        super(itemView);
        if(itemView!=null) {
            leftMsgLayout = (LinearLayout) itemView.findViewById(R.id.chat_left_msg_layout);
            rightMsgLayout = (LinearLayout) itemView.findViewById(R.id.chat_right_msg_layout);
            leftMsgTextView = (TextView) itemView.findViewById(R.id.chat_left_msg_text_view);
            rightMsgTextView = (TextView) itemView.findViewById(R.id.chat_right_msg_text_view);
            devicenameLeft = (TextView) itemView.findViewById(R.id.devicenameLeft);
            devicenamYou = (TextView) itemView.findViewById(R.id.devicenameYou);
            itemImage = itemView.findViewById(R.id.itemImage);
        }
    }
}