package lyon.kevin.bluetooth.assistant.chat.NetWork.WifiSetting;

import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

import lyon.kevin.bluetooth.assistant.chat.R;


/**
 * Created by i_chihhsuanwang on 2016/12/27.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    public List<ScanResult> datas = null;

    public RecyclerAdapter(List<ScanResult> datas) {this.datas = datas;}

    public interface OnItemClickListener {
        void onItemClick(View view, int position, ScanResult data);
        void onItemLongClick(View view, int position);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }


    //创建新View，被LayoutManager所调用
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.wifi_mune_item,viewGroup,false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }
    //根據數據將wifi的強度做區分(包含SSID)
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        viewHolder.mTextView.setText(datas.get(position).SSID.toString());
        int level = Integer.valueOf(datas.get(position).level).intValue();
        if (level <= 0 && level >= -50 ) {
            viewHolder.mImageview.setImageResource(R.drawable.icon_ge);
        }else if (level < -50 && level >= -70){
            viewHolder.mImageview.setImageResource(R.drawable.icon_y);
        }else if (level < -70 && level >= -80){
            viewHolder.mImageview.setImageResource(R.drawable.icon_o);
        }else if (level < -80 && level >= -100){
            viewHolder.mImageview.setImageResource(R.drawable.icon_r);
        }

        if( onItemClickListener!= null){
            viewHolder.itemView.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //避免更新list順綠導致拿到錯誤順序
                    int layoutPosition = viewHolder.getAdapterPosition();
                    onItemClickListener.onItemClick(viewHolder.itemView, layoutPosition,datas.get(position));
                }
            });

            viewHolder.itemView.setOnLongClickListener( new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int layoutPosition = viewHolder.getAdapterPosition();
                    onItemClickListener.onItemLongClick(viewHolder.itemView, layoutPosition);
                    return false;
                }
            });
        }
    }
    //获取数据的数量
    @Override
    public int getItemCount() {
        return datas.size();
    }
    //自定义的ViewHolder，持有每个Item的的所有界面元素
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public ImageView mImageview;
        public ViewHolder(View view){
            super(view);
            mTextView = (TextView) view.findViewById(R.id.textView6);
            mImageview = (ImageView) view.findViewById(R.id.imageView2);
        }
    }


}
