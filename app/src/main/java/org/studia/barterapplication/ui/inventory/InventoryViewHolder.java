package org.studia.barterapplication.ui.inventory;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.studia.barterapplication.R;
import org.studia.barterapplication.inventory.Inventory;

public class InventoryViewHolder extends RecyclerView.ViewHolder {
    private View view;
    private TextView inventoryName;
    private ImageView itemPreview;
    private OnClickListener onClickListener;

    public InventoryViewHolder(@NonNull View itemView) {
        super(itemView);
        this.view = itemView;
        itemPreview = view.findViewById(R.id.item_preview_photo);
        inventoryName = view.findViewById(R.id.inventory_name);
        itemView.setOnClickListener(
                view -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onClickListener.onClick(position);
                    }
                }
        );
    }

    public View getView() {
        return view;
    }

    public void bindData(Inventory inventory) {
        inventoryName.setText(inventory.getName());
        if (!"default".equals(inventory.getPhotoUrl())) {
            Glide.with(getView().getRootView().getContext())
                    .load(inventory.getPhotoUrl())
                    .into(itemPreview);
        }
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }


    public interface OnClickListener {
        void onClick(int position);
    }
}
