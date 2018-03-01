package com.example.mymemo;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Created by xianglei on 2018/1/3.
 */

public class MemoAdapter extends RecyclerView.Adapter<MemoAdapter.ViewHolder> {

    private OnSaveListener mOnSaveListener;

    private List<ContentItem> mMemoList;

    private Context mContext;

    private Remind remind;

    private int layout;

    ContentItem content;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View memoView;
        TextView date;
        TextView content;
        Switch remind;
        Button delete, reserve;
        public ViewHolder(View view) {
            super(view);
            memoView = view;
            date = (TextView) view.findViewById(R.id.memo_date);
            content = (TextView) view.findViewById(R.id.memo_content);
            remind = (Switch) view.findViewById(R.id.remind);
            delete = (Button) view.findViewById(R.id.delete);
            reserve = (Button) view.findViewById(R.id.reserve);
        }
    }
    public MemoAdapter(List<ContentItem> memoList, Context context, int layout) {
        mMemoList = memoList;
        mContext = context;
        this.layout = layout;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int ViewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent,
                false);
        final ViewHolder holder = new ViewHolder(view);
        holder.memoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    int position = holder.getAdapterPosition();
                    ContentItem contentData = mMemoList.get(position);
                    int id = contentData.getId();
                    DisplayContent.actionStart(mContext, id);
            }
        });

        holder.remind.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                int position = holder.getAdapterPosition();
                final ContentItem contentData = mMemoList.get(position);
                if (b) {
                    if(contentData.getIsRemind() != 1)  {
                        remind = new Remind(mContext, contentData.getId());
                        remind.setCancelCallBack(new Remind.CancelCallBack() {
                            @Override
                            public void cancel() {
                                Toast.makeText(mContext, "没有设置提醒，已取消", Toast.LENGTH_SHORT).show();
                                holder.remind.setChecked(false);
                            }
                            @Override
                            public void decision(long date) {
                                contentData.setIsRemind(1);
                                notifyDataSetChanged();
                            }
                        });
                    }
                } else if(contentData.getIsRemind() == 1) {
                    MyDatabase.DB.execSQL(String.format("update Memo set isRemind = %d where id = %d",
                            0, contentData.getId()));
                    RemindService.actionStart(mContext, contentData.getId(), true);
                    contentData.setIsRemind(0);
                    optionDialog(contentData);

                }
            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(mContext, R.style.AlertDialog);
                dialog.setTitle("删除");
                dialog.setMessage("删除后无法恢复，请慎重！！！");
                dialog.setCancelable(true);
                dialog.setNegativeButton("不做了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int position = holder.getAdapterPosition();
                        final ContentItem contentData = mMemoList.get(position);
                        int id = contentData.getId();
                        MyDatabase.DB.execSQL(String.format("delete from memo where id = %d",
                                id));
                        File file = new File(Directory.getDirectory(mContext, id, Directory.DIRECTORY));
                        EditActivity.deleteFile(file);
                        mMemoList.remove(position);
                        notifyDataSetChanged();
                    }
                });
                dialog.setPositiveButton("已完成", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int position = holder.getAdapterPosition();
                        final ContentItem contentData = mMemoList.get(position);
                        int id = contentData.getId();
                        MyDatabase.DB.execSQL(String.format("delete from memo where id = %d",
                                id));
                        File file = new File(Directory.getDirectory(mContext, id, Directory.DIRECTORY));
                        EditActivity.deleteFile(file);
                        mMemoList.remove(position);
                        notifyDataSetChanged();
                        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
                        SharedPreferences.Editor edit = pref.edit();
                        int quantityCompletion = 0;
                        if(pref != null) {
                            quantityCompletion = pref.getInt("quantity_completion", 0) + 1;
                            Toast.makeText(mContext, "您已经完成了" + quantityCompletion + "件事, 请继续努力！",
                                    Toast.LENGTH_SHORT).show();
                            edit.putInt("quantity_completion", quantityCompletion);
                            edit.apply();
                        }
                    }
                });
                dialog.setNeutralButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                dialog.show();
            }
        });
        holder.reserve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                final ContentItem contentData = mMemoList.get(position);
                int id = contentData.getId();
                MyDatabase.DB.execSQL(String.format("update Memo set is_lay_up = 1 where id = %d", id));
                mMemoList.remove(position);
                notifyDataSetChanged();
                if(mOnSaveListener != null) {
                    mOnSaveListener.onClick();
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        content = mMemoList.get(position);
        String date = content.getDate();
        holder.content.setText(content.getContent());
        if(content.getIsRemind() == 0) {
            holder.remind.setChecked(false);
        } else {
            holder.remind.setChecked(true);
        }
        if(content.getIsLayUp() == 1) {
            holder.reserve.setVisibility(View.GONE);
            holder.date.setText(date);
        } else {
            holder.date.setText(date.substring(content.getDate().indexOf("-") + 1));
        }
    }

    @Override
    public int getItemCount() {
        return mMemoList.size();
    }

    public interface OnSaveListener {
        void onClick();
    }

    public void setOnSaveListener(OnSaveListener callback) {
        mOnSaveListener = callback;
    }

    private void optionRemind(long time, ContentItem content) {
        long date = new Date(System.currentTimeMillis()).getTime() + time;
        RemindService.actionStart(mContext, content.getId(), date, content.getDate(), content.getContent());
        MyDatabase.DB.execSQL(String.format("update Memo set remind_date = %d, isRemind = %d where id = %d",
                date, 1, content.getId()));
    }

    private void optionDialog(final ContentItem content) {
        final String[] items = new String[] {"10分钟后提醒！", "30分钟后提醒！",
                "1小时后提醒！", "2小时后提醒！"};
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialog);
        builder.setTitle("是否继续提醒？");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(items[which]) {
                    case "10分钟后提醒！" :
                        optionRemind(10 * 60 * 1000, content);
                        content.setIsRemind(1);
                        notifyDataSetChanged();
                        break;
                    case "30分钟后提醒！" :
                        optionRemind(30 * 60 * 1000, content);
                        content.setIsRemind(1);
                        notifyDataSetChanged();
                        break;
                    case "1小时后提醒！" :
                        optionRemind(60 * 60 * 1000, content);
                        content.setIsRemind(1);
                        notifyDataSetChanged();
                        break;
                    case "2小时后提醒！" :
                        optionRemind(2 * 60 * 60 * 1000, content);
                        content.setIsRemind(1);
                        notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
            }
        });
        builder.setCancelable(false);
        builder.setPositiveButton("暂不提醒", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.create().show();
    }
}
