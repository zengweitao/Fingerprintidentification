package sunrun.fingerprintidentification.view;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import sunrun.fingerprintidentification.R;


/**
 * Created by dell on 2016/5/26.
 */
public abstract class DialogView extends CustomDialog {

    private Context mContext;
    private int tag = CANCELTAG;
    public static final int OKTAG = 0;
    public static final int CANCELTAG = 1;

    public DialogView(Context context, int width, int height, int layout, int style) {
        super(context, width, height, layout, style);

    }

    public DialogView(Context context, int layout, int style) {
        super(context, layout, style);
    }

    public DialogView(Context context) {
        super(context, R.layout.my_dialog, R.style.dialog);
    }

    public abstract void isdismiss(int tag);

    //默认提示文字的对话框
    public void showdialog2(String title, String boby, String text_cancel, String text_ok) {

        TextView dialog_title = (TextView) this
                .findViewById(R.id.dialog_title);
        TextView dialog_message = (TextView) this
                .findViewById(R.id.dialog_message);
        TextView dialog_ok = (TextView) this.findViewById(R.id.dialog_ok);
        TextView dialog_cancel = (TextView) this
                .findViewById(R.id.dialog_cancel);
        ImageView include_hine = (ImageView) this.findViewById(R.id.include_hine);
        this.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isdismiss(tag);
            }
        });
        if (!TextUtils.isEmpty(text_cancel)) {
            dialog_cancel.setVisibility(View.VISIBLE);
            dialog_cancel.setText(text_cancel);
            dialog_cancel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    tag = CANCELTAG;
                    DialogView.this.dismiss();
                }
            });
        } else {
            include_hine.setVisibility(View.GONE);
            dialog_cancel.setVisibility(View.GONE);
        }
        dialog_title.setText(title);
        dialog_message.setText(boby);
        if (!TextUtils.isEmpty(text_ok)) {
            dialog_ok.setVisibility(View.VISIBLE);
            dialog_ok.setText(text_ok);
            dialog_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tag = OKTAG;
                    DialogView.this.dismiss();
                }
            });
        } else {
            dialog_ok.setVisibility(View.GONE);
            include_hine.setVisibility(View.GONE);
        }

        this.show();
    }

    public String getDialog_editText() {

        return dialog_editText == null ? "" : dialog_editText;
    }

    public void setDialog_editText(EditText dialog_editText) {
        if (dialog_editText == null) {
            this.dialog_editText = "";
        } else {
            this.dialog_editText = dialog_editText.getText().toString();
        }
    }

    private String dialog_editText;

    //带有编辑框的对话框
    public void showdialog(String title, String boby, String text_cancel, String text_ok) {

        TextView dialog_title = (TextView) this
                .findViewById(R.id.dialog_title);
        TextView dialog_message = (TextView) this
                .findViewById(R.id.dialog_message);
        TextView dialog_ok = (TextView) this.findViewById(R.id.dialog_ok);
        TextView dialog_cancel = (TextView) this
                .findViewById(R.id.dialog_cancel);
        final EditText dialog_editText = (EditText) this
                .findViewById(R.id.dialog_editText);

        this.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isdismiss(tag);
            }
        });
        if (!TextUtils.isEmpty(text_cancel)) {
            dialog_cancel.setVisibility(View.VISIBLE);
            dialog_cancel.setText(text_cancel);
            dialog_cancel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    tag = CANCELTAG;
                    DialogView.this.dismiss();
                }
            });
        } else {
            dialog_cancel.setVisibility(View.GONE);
        }
        dialog_title.setText(title);
        dialog_message.setText(boby);
        if (!TextUtils.isEmpty(text_ok)) {
            dialog_ok.setVisibility(View.VISIBLE);
            dialog_ok.setText(text_ok);
            //备注: 有编辑框的对话框点击"确定"手动控制对话框是否消失
            dialog_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setDialog_editText(dialog_editText);
                    tag = OKTAG;
                    DialogView.this.dismiss();
                }
            });
        } else {
            dialog_ok.setVisibility(View.GONE);
        }

        this.show();
    }
}
