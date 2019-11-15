package lyon.kevin.bluetooth.assistant.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatAdapter extends BaseAdapter {
    String TAG = ChatAdapter.class.getSimpleName();
    Context context;
    ArrayList<HashMap<String,String>> chatMessages;
    TextView textView;
    public ChatAdapter(Context context, ArrayList<HashMap<String,String>> chatMessages){
        this.context=context;
        this.chatMessages=chatMessages;
    }

    @Override
    public int getCount() {
        return chatMessages.size();
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
        textView = (TextView)view.findViewById(R.id.chat_txt);
        final String LOGd="Logd";
        final String LOGe="Loge";
        final String LOGi="Logi";
        final String LOGw="Logw";
        final String LOGv="Logv";
        String Typestr = chatMessages.get(i).get("Type");
        String msg = chatMessages.get(i).get("MSG");

        textView.setText(msg);
        switch (Typestr){
            case LOGd:
                textView.setTextColor(context.getResources().getColor(R.color.gray));
                Log.d(TAG,"Typestr:"+Typestr+"\n  msg="+msg+" block1");
                break;
            case LOGe:
                textView.setTextColor(context.getResources().getColor(R.color.red));
                Log.d(TAG,"Typestr:"+Typestr+"\n  msg="+msg+" red");
                break;
            case LOGi:
                textView.setTextColor(context.getResources().getColor(R.color.yellow));
                Log.d(TAG,"Typestr:"+Typestr+"\n  msg="+msg+" yellow");
                break;
            case LOGw:
                textView.setTextColor(context.getResources().getColor(R.color.green));
                Log.d(TAG,"Typestr:"+Typestr+"\n  msg="+msg+" green");
                break;
            case LOGv:
                textView.setTextColor(context.getResources().getColor(R.color.blue));
                Log.d(TAG,"Typestr:"+Typestr+"\n msg="+msg+" blue");
                break;
            default:
                textView.setTextColor(context.getResources().getColor(R.color.block));
                Log.d(TAG,"Typestr:"+Typestr+"\n  msg="+msg+" block2");
                break;
        }

        return view;
    }
}
