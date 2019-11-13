package lyon.kevin.bluetooth.assistant.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

public class ChatAdapter extends BaseAdapter {
    Context context;
    ArrayList<String> chatMessages;
    public ChatAdapter(Context context, ArrayList<String> chatMessages){
        this.context=context;
        this.chatMessages=chatMessages;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_item,viewGroup,false);
        return view;
    }
}
