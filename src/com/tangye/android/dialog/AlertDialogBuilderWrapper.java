package com.tangye.android.dialog;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;

import com.cnepay.android.pos2.R;
import com.tangye.android.utils.PublicHelper;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.DialogInterface.OnShowListener;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

public class AlertDialogBuilderWrapper implements OnShowListener {

	Window window;
	Builder builder;
	Context mContext;
	
	public AlertDialogBuilderWrapper(Context ctx) {
		builder = new Builder(ctx);
		mContext = ctx;
	}
	
	public AlertDialogBuilderWrapper(Context ctx, int theme) {
		//ctx = new ContextThemeWrapper(ctx, android.R.style.Theme_Light);
    	try {
			Class<?> b;
			b = Class.forName("android.app.AlertDialog$Builder");
			Constructor<?> c = b.getConstructor(Context.class, int.class);
			builder = (Builder) c.newInstance(ctx, theme);
    	} catch(Exception e) {
    		//e.printStackTrace();
    		builder = new Builder(new ContextThemeWrapper(ctx, theme));
    	}
    	mContext = ctx;
    	
	}
	
	public AlertDialog create() {
		final AlertDialog dialog = builder.create();
		dialog.setOnShowListener(this);
		window = dialog.getWindow();
		return dialog;
	}
	
	//wrapper **************
	public Context getContext() {
        return mContext;
    }

    /**
     * Set the title using the given resource id.
     *
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public AlertDialogBuilderWrapper setTitle(int titleId) {
        builder.setTitle(titleId);
        return this;
    }
    
    /**
     * Set the title displayed in the {@link Dialog}.
     *
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public AlertDialogBuilderWrapper setTitle(CharSequence title) {
        builder.setTitle(title);
        return this;
    }
    
    /**
     * Set the title using the custom view {@code customTitleView}. The
     * methods {@link #setTitle(int)} and {@link #setIcon(int)} should be
     * sufficient for most titles, but this is provided if the title needs
     * more customization. Using this will replace the title and icon set
     * via the other methods.
     * 
     * @param customTitleView The custom view to use as the title.
     *
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public AlertDialogBuilderWrapper setCustomTitle(View customTitleView) {
        builder.setCustomTitle(customTitleView);
        return this;
    }
    
    /**
     * Set the message to display using the given resource id.
     *
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public AlertDialogBuilderWrapper setMessage(int messageId) {
        builder.setMessage(messageId);
        return this;
    }
    
    /**
     * Set the message to display.
      *
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public AlertDialogBuilderWrapper setMessage(CharSequence message) {
    	builder.setMessage(message);
        return this;
    }
    
    /**
     * Set the resource id of the {@link Drawable} to be used in the title.
     *
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public AlertDialogBuilderWrapper setIcon(int iconId) {
    	switch(iconId) {
    	case android.R.drawable.ic_dialog_info:
    		iconId = R.drawable.info;
    		break;
    	case android.R.drawable.ic_dialog_alert:
    		iconId = R.drawable.alert;
    		break;
    	}
        builder.setIcon(iconId);
        return this;
    }
    
    /**
     * Set the {@link Drawable} to be used in the title.
      *
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public AlertDialogBuilderWrapper setIcon(Drawable icon) {
        builder.setIcon(icon);
        return this;
    }

    /**
     * Set a listener to be invoked when the positive button of the dialog is pressed.
     * @param textId The resource id of the text to display in the positive button
     * @param listener The {@link DialogInterface.OnClickListener} to use.
     *
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public AlertDialogBuilderWrapper setPositiveButton(int textId, final OnClickListener listener) {
        builder.setPositiveButton(textId, listener);
        return this;
    }
    
    /**
     * Set a listener to be invoked when the positive button of the dialog is pressed.
     * @param text The text to display in the positive button
     * @param listener The {@link DialogInterface.OnClickListener} to use.
     *
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public AlertDialogBuilderWrapper setPositiveButton(CharSequence text, final OnClickListener listener) {
        builder.setPositiveButton(text, listener);
        return this;
    }
    
    /**
     * Set a listener to be invoked when the negative button of the dialog is pressed.
     * @param textId The resource id of the text to display in the negative button
     * @param listener The {@link DialogInterface.OnClickListener} to use.
     *
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public AlertDialogBuilderWrapper setNegativeButton(int textId, final OnClickListener listener) {
        builder.setNegativeButton(textId, listener);
        return this;
    }
    
    /**
     * Set a listener to be invoked when the negative button of the dialog is pressed.
     * @param text The text to display in the negative button
     * @param listener The {@link DialogInterface.OnClickListener} to use.
     *
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public AlertDialogBuilderWrapper setNegativeButton(CharSequence text, final OnClickListener listener) {
        builder.setNegativeButton(text, listener);
        return this;
    }
    
    /**
     * Set a listener to be invoked when the neutral button of the dialog is pressed.
     * @param textId The resource id of the text to display in the neutral button
     * @param listener The {@link DialogInterface.OnClickListener} to use.
     *
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public AlertDialogBuilderWrapper setNeutralButton(int textId, final OnClickListener listener) {
        builder.setNeutralButton(textId, listener);
        return this;
    }
    
    /**
     * Set a listener to be invoked when the neutral button of the dialog is pressed.
     * @param text The text to display in the neutral button
     * @param listener The {@link DialogInterface.OnClickListener} to use.
     *
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public AlertDialogBuilderWrapper setNeutralButton(CharSequence text, final OnClickListener listener) {
        builder.setNeutralButton(text, listener);
        return this;
    }
    
    /**
     * Sets whether the dialog is cancelable or not.  Default is true.
     *
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public AlertDialogBuilderWrapper setCancelable(boolean cancelable) {
        builder.setCancelable(cancelable);
        return this;
    }
    
    /**
     * Sets the callback that will be called if the dialog is canceled.
     * @see #setCancelable(boolean)
     *
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public AlertDialogBuilderWrapper setOnCancelListener(OnCancelListener onCancelListener) {
        builder.setOnCancelListener(onCancelListener);
        return this;
    }
    
    /**
     * Sets the callback that will be called if a key is dispatched to the dialog.
     *
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public AlertDialogBuilderWrapper setOnKeyListener(OnKeyListener onKeyListener) {
        builder.setOnKeyListener(onKeyListener);
        return this;
    }
    
    /**
     * Set a list of items to be displayed in the dialog as the content, you will be notified of the
     * selected item via the supplied listener. This should be an array type i.e. R.array.foo
     *
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public AlertDialogBuilderWrapper setItems(int itemsId, final OnClickListener listener) {
        builder.setItems(itemsId, listener);
        return this;
    }
    
    /**
     * Set a list of items to be displayed in the dialog as the content, you will be notified of the
     * selected item via the supplied listener.
     *
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public AlertDialogBuilderWrapper setItems(CharSequence[] items, final OnClickListener listener) {
        builder.setItems(items, listener);
        return this;
    }
    
    /**
     * Set a list of items, which are supplied by the given {@link ListAdapter}, to be
     * displayed in the dialog as the content, you will be notified of the
     * selected item via the supplied listener.
     * 
     * @param adapter The {@link ListAdapter} to supply the list of items
     * @param listener The listener that will be called when an item is clicked.
     *
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public AlertDialogBuilderWrapper setAdapter(final ListAdapter adapter, final OnClickListener listener) {
        builder.setAdapter(adapter, listener);
        return this;
    }
    
    /**
     * Set a list of items, which are supplied by the given {@link Cursor}, to be
     * displayed in the dialog as the content, you will be notified of the
     * selected item via the supplied listener.
     * 
     * @param cursor The {@link Cursor} to supply the list of items
     * @param listener The listener that will be called when an item is clicked.
     * @param labelColumn The column name on the cursor containing the string to display
     *          in the label.
     *
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public AlertDialogBuilderWrapper setCursor(final Cursor cursor, final OnClickListener listener,
            String labelColumn) {
        builder.setCursor(cursor, listener, labelColumn);
        return this;
    }
    
    /**
     * Set a list of items to be displayed in the dialog as the content,
     * you will be notified of the selected item via the supplied listener.
     * This should be an array type, e.g. R.array.foo. The list will have
     * a check mark displayed to the right of the text for each checked
     * item. Clicking on an item in the list will not dismiss the dialog.
     * Clicking on a button will dismiss the dialog.
     * 
     * @param itemsId the resource id of an array i.e. R.array.foo
     * @param checkedItems specifies which items are checked. It should be null in which case no
     *        items are checked. If non null it must be exactly the same length as the array of
     *        items.
     * @param listener notified when an item on the list is clicked. The dialog will not be
     *        dismissed when an item is clicked. It will only be dismissed if clicked on a
     *        button, if no buttons are supplied it's up to the user to dismiss the dialog.
     *
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public AlertDialogBuilderWrapper setMultiChoiceItems(int itemsId, boolean[] checkedItems, 
            final OnMultiChoiceClickListener listener) {
        builder.setMultiChoiceItems(itemsId, checkedItems, listener);
        return this;
    }
    
    /**
     * Set a list of items to be displayed in the dialog as the content,
     * you will be notified of the selected item via the supplied listener.
     * The list will have a check mark displayed to the right of the text
     * for each checked item. Clicking on an item in the list will not
     * dismiss the dialog. Clicking on a button will dismiss the dialog.
     * 
     * @param items the text of the items to be displayed in the list.
     * @param checkedItems specifies which items are checked. It should be null in which case no
     *        items are checked. If non null it must be exactly the same length as the array of
     *        items.
     * @param listener notified when an item on the list is clicked. The dialog will not be
     *        dismissed when an item is clicked. It will only be dismissed if clicked on a
     *        button, if no buttons are supplied it's up to the user to dismiss the dialog.
     *
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public AlertDialogBuilderWrapper setMultiChoiceItems(CharSequence[] items, boolean[] checkedItems, 
            final OnMultiChoiceClickListener listener) {
        builder.setMultiChoiceItems(items, checkedItems, listener);
        return this;
    }
    
    /**
     * Set a list of items to be displayed in the dialog as the content,
     * you will be notified of the selected item via the supplied listener.
     * The list will have a check mark displayed to the right of the text
     * for each checked item. Clicking on an item in the list will not
     * dismiss the dialog. Clicking on a button will dismiss the dialog.
     * 
     * @param cursor the cursor used to provide the items.
     * @param isCheckedColumn specifies the column name on the cursor to use to determine
     *        whether a checkbox is checked or not. It must return an integer value where 1
     *        means checked and 0 means unchecked.
     * @param labelColumn The column name on the cursor containing the string to display in the
     *        label.
     * @param listener notified when an item on the list is clicked. The dialog will not be
     *        dismissed when an item is clicked. It will only be dismissed if clicked on a
     *        button, if no buttons are supplied it's up to the user to dismiss the dialog.
     *
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public AlertDialogBuilderWrapper setMultiChoiceItems(Cursor cursor, String isCheckedColumn, String labelColumn, 
            final OnMultiChoiceClickListener listener) {
        builder.setMultiChoiceItems(cursor, isCheckedColumn, labelColumn, listener);
        return this;
    }
    
    /**
     * Set a list of items to be displayed in the dialog as the content, you will be notified of
     * the selected item via the supplied listener. This should be an array type i.e.
     * R.array.foo The list will have a check mark displayed to the right of the text for the
     * checked item. Clicking on an item in the list will not dismiss the dialog. Clicking on a
     * button will dismiss the dialog.
     * 
     * @param itemsId the resource id of an array i.e. R.array.foo
     * @param checkedItem specifies which item is checked. If -1 no items are checked.
     * @param listener notified when an item on the list is clicked. The dialog will not be
     *        dismissed when an item is clicked. It will only be dismissed if clicked on a
     *        button, if no buttons are supplied it's up to the user to dismiss the dialog.
     *
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public AlertDialogBuilderWrapper setSingleChoiceItems(int itemsId, int checkedItem, 
            final OnClickListener listener) {
        builder.setSingleChoiceItems(itemsId, checkedItem, listener);
        return this;
    }
    
    /**
     * Set a list of items to be displayed in the dialog as the content, you will be notified of
     * the selected item via the supplied listener. The list will have a check mark displayed to
     * the right of the text for the checked item. Clicking on an item in the list will not
     * dismiss the dialog. Clicking on a button will dismiss the dialog.
     * 
     * @param cursor the cursor to retrieve the items from.
     * @param checkedItem specifies which item is checked. If -1 no items are checked.
     * @param labelColumn The column name on the cursor containing the string to display in the
     *        label.
     * @param listener notified when an item on the list is clicked. The dialog will not be
     *        dismissed when an item is clicked. It will only be dismissed if clicked on a
     *        button, if no buttons are supplied it's up to the user to dismiss the dialog.
     *
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public AlertDialogBuilderWrapper setSingleChoiceItems(Cursor cursor, int checkedItem, String labelColumn, 
            final OnClickListener listener) {
        builder.setSingleChoiceItems(cursor, checkedItem, labelColumn, listener);
        return this;
    }
    
    /**
     * Set a list of items to be displayed in the dialog as the content, you will be notified of
     * the selected item via the supplied listener. The list will have a check mark displayed to
     * the right of the text for the checked item. Clicking on an item in the list will not
     * dismiss the dialog. Clicking on a button will dismiss the dialog.
     * 
     * @param items the items to be displayed.
     * @param checkedItem specifies which item is checked. If -1 no items are checked.
     * @param listener notified when an item on the list is clicked. The dialog will not be
     *        dismissed when an item is clicked. It will only be dismissed if clicked on a
     *        button, if no buttons are supplied it's up to the user to dismiss the dialog.
     *
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public AlertDialogBuilderWrapper setSingleChoiceItems(CharSequence[] items, int checkedItem, final OnClickListener listener) {
        builder.setSingleChoiceItems(items, checkedItem, listener);
        return this;
    } 
    
    /**
     * Set a list of items to be displayed in the dialog as the content, you will be notified of
     * the selected item via the supplied listener. The list will have a check mark displayed to
     * the right of the text for the checked item. Clicking on an item in the list will not
     * dismiss the dialog. Clicking on a button will dismiss the dialog.
     * 
     * @param adapter The {@link ListAdapter} to supply the list of items
     * @param checkedItem specifies which item is checked. If -1 no items are checked.
     * @param listener notified when an item on the list is clicked. The dialog will not be
     *        dismissed when an item is clicked. It will only be dismissed if clicked on a
     *        button, if no buttons are supplied it's up to the user to dismiss the dialog.
     *
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public AlertDialogBuilderWrapper setSingleChoiceItems(ListAdapter adapter, int checkedItem, final OnClickListener listener) {
        builder.setSingleChoiceItems(adapter, checkedItem, listener);
        return this;
    }
    
    /**
     * Sets a listener to be invoked when an item in the list is selected.
     * 
     * @param listener The listener to be invoked.
     * @see AdapterView#setOnItemSelectedListener(android.widget.AdapterView.OnItemSelectedListener)
     *
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public AlertDialogBuilderWrapper setOnItemSelectedListener(final AdapterView.OnItemSelectedListener listener) {
        builder.setOnItemSelectedListener(listener);
        return this;
    }
    
    /**
     * Set a custom view to be the contents of the Dialog. If the supplied view is an instance
     * of a {@link ListView} the light background will be used.
     *
     * @param view The view to use as the contents of the Dialog.
     * 
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public AlertDialogBuilderWrapper setView(View view) {
    	builder.setView(view);
        return this;
    }
    
    /**
     * Sets the Dialog to use the inverse background, regardless of what the
     * contents is.
     * 
     * @param useInverseBackground Whether to use the inverse background
     * 
     * @return This Builder object to allow for chaining of calls to set methods
     */
    public AlertDialogBuilderWrapper setInverseBackgroundForced(boolean useInverseBackground) {
        builder.setInverseBackgroundForced(useInverseBackground);
        return this;
    }

    /**
     * Creates a {@link AlertDialog} with the arguments supplied to this builder and
     * {@link Dialog#show()}'s the dialog.
     */
    public AlertDialog show() {
        AlertDialog dialog = create();
        dialog.show();
        return dialog;
    }

	@Override
	public void onShow(DialogInterface dialog) {
		try {
			WindowManager.LayoutParams winlp = window.getAttributes();
			winlp.width = WindowManager.LayoutParams.MATCH_PARENT;
			window.setAttributes(winlp);
			Class<?> b;
			b = Class.forName("com.android.internal.R$id");
			//parentPanel
			//topPanel
			//titleDivider
			//contentPanel
			//scrollView
			//message
			//customPanel
			//buttonPanel
			//alertTitle
			//leftSpacer
			//rightSpacer
			Field f;
			int id;
			LinearLayout.LayoutParams lp;
			/*
			for (Field f1 : b.getDeclaredFields()) {
				android.util.Log.w("ty", f1.getName());
			}
			*/
			View parent = null;
			try {
				f = b.getDeclaredField("parentPanel");
				id = (Integer) f.get(null);
				parent = window.findViewById(id);
			} catch (NoSuchFieldException e) {}
			
			f = b.getDeclaredField("topPanel");
			id = (Integer) f.get(null);
			View top = window.findViewById(id);
			if (parent == null) {
				// fix for jelly bean
				parent = top.getRootView();
			}
			
			f = b.getDeclaredField("title_template");
			id = (Integer) f.get(null);
			View titletemp = window.findViewById(id);
			
			f = b.getDeclaredField("icon");
			id = (Integer) f.get(null);
			View icon = window.findViewById(id);
			
			f = b.getDeclaredField("titleDivider");
			id = (Integer) f.get(null);
			View divider = window.findViewById(id);
			
			f = b.getDeclaredField("alertTitle");
			id = (Integer) f.get(null);
			TextView title = (TextView) window.findViewById(id);
			
			f = b.getDeclaredField("scrollView");
			id = (Integer) f.get(null);
			View scroll = window.findViewById(id);
			
			f = b.getDeclaredField("message");
			id = (Integer) f.get(null);
			TextView msg = (TextView) window.findViewById(id);
			
			f = b.getDeclaredField("buttonPanel");
			id = (Integer) f.get(null);
			ViewGroup button = (ViewGroup) window.findViewById(id);
			
			f = b.getDeclaredField("leftSpacer");
			id = (Integer) f.get(null);
			View left = window.findViewById(id);
			
			f = b.getDeclaredField("rightSpacer");
			id = (Integer) f.get(null);
			View right = window.findViewById(id);

			if (parent != null) {
				int m = PublicHelper.dp2px(mContext, 8);
				parent.setPadding(m, m, m, m);
			}
			
			if (top != null) {
				top.setMinimumHeight(PublicHelper.dp2px(mContext, 54));
				top.setPadding(top.getPaddingLeft(), top.getPaddingTop(), top.getPaddingRight(), 0);
				lp = (LinearLayout.LayoutParams) top.getLayoutParams();
				lp.gravity = Gravity.CENTER_VERTICAL;
				top.setLayoutParams(lp);
			}
			
			if (titletemp != null) {
				int m = PublicHelper.dp2px(mContext, 8);
				int n = PublicHelper.dp2px(mContext, 12);
				titletemp.setPadding(m, n, m, n);
				lp = (LinearLayout.LayoutParams) titletemp.getLayoutParams();
				lp.topMargin = 0;
				lp.bottomMargin = 0;
				titletemp.setLayoutParams(lp);
			}
			
			if (icon != null) {
				icon.setPadding(0, 0, PublicHelper.dp2px(mContext, 8), 0);
				lp = (LinearLayout.LayoutParams) icon.getLayoutParams();
				lp.gravity = Gravity.NO_GRAVITY;
				icon.setLayoutParams(lp);
			}
			
			if (divider != null) {
				lp = (LinearLayout.LayoutParams) divider.getLayoutParams();
				lp.height = PublicHelper.dp2px(mContext, 2);
				divider.setVisibility(View.VISIBLE);
				divider.setLayoutParams(lp);
				if (divider instanceof ImageView) {
					ImageView img = (ImageView) divider;
					img.setImageResource(0);
				}
				divider.setBackgroundColor(0xff279ce7);
			}
			
			if (title != null) {
				title.setTextColor(0xff279ce7);
				title.setPadding(0, 0, 0, 0);
				title.setTextSize(PublicHelper.dp2px(mContext, 16));
				title.setShadowLayer(0, 0, 0, 0);
			}
			
			if (scroll != null) {
				scroll.setPadding(16, 8, 16, 8);
			}
			
			if (msg != null) {
				int n = PublicHelper.dp2px(mContext, 8);
				msg.setPadding(n, 0, n, 0);
				msg.setTextColor(0xff000000);
				msg.setGravity(Gravity.CENTER_VERTICAL);
				msg.setMinimumHeight(PublicHelper.dp2px(mContext, 40));
			}
			
			if (button != null) {
				button.setPadding(button.getPaddingLeft(), 0, button.getPaddingRight(), button.getPaddingBottom());
				if (left != null) {
					left.setVisibility(View.GONE);
				}
				if (right != null) {
					right.setVisibility(View.GONE);
				}
				ViewGroup box = (ViewGroup) button.getChildAt(0);
				if (box != null) {
					box.setPadding(0, 0, 0, 0);
					box.setBackgroundColor(0);
					ArrayList<Button> allbtn = new ArrayList<Button>();
					for (int i = 0; i < box.getChildCount(); i++) {
						View v = box.getChildAt(i);
						if (v instanceof Button) {
							if (v.getVisibility() == View.VISIBLE) {
								Button btn = (Button)v;
								allbtn.add(btn);
								btn.setMinimumHeight(PublicHelper.dp2px(mContext, 54));
								btn.setTextSize(PublicHelper.dp2px(mContext, 12));
								//btn.setTextColor(0xff279ce7);
								ColorStateList csl = mContext.getResources().getColorStateList(R.drawable.dialog_button_txt);
								btn.setTextColor(csl);
							}
						}
					}
					int num = allbtn.size();
					if (num == 1) {
						allbtn.get(0).setBackgroundResource(R.drawable.dialogbtn);
					} else if (num >= 2) {
						allbtn.get(0).setBackgroundResource(R.drawable.dialogbtn_left);
						allbtn.get(num - 1).setBackgroundResource(R.drawable.dialogbtn_right);
						if (num == 3) {
							allbtn.get(1).setBackgroundResource(R.drawable.dialogbtn_mid);
						}
					}
				}
			}
			
			parent.requestLayout();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
