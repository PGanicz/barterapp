package org.studia.barterapplication.ui.inventory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import org.studia.barterapplication.R;
import org.studia.barterapplication.inventory.Inventory;

public class InventoryAdapter extends FirestoreRecyclerAdapter<Inventory, InventoryViewHolder> {

    private OnClickListener onClickListener;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public InventoryAdapter(@NonNull FirestoreRecyclerOptions<Inventory> options) {
        super(options);
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent,
                false);
        return new InventoryViewHolder(view);
    }

    public void deleteItem(int position) {
        DocumentReference reference = getSnapshots().getSnapshot(position).getReference();
        FirebaseStorage.getInstance().getReference()
                .child("items")
                .child(reference.getId() + ".jpeg")
                .delete();
        reference.delete();
    }

    @Override
    protected void onBindViewHolder(@NonNull InventoryViewHolder holder, int position, @NonNull Inventory model) {
        holder.bindData(model);
        holder.setOnClickListener(pos -> onClickListener.onClick(getSnapshots().getSnapshot(pos)));
        holder.getView().setOnLongClickListener(e -> true);
    }

    public interface OnClickListener {
        void onClick(DocumentSnapshot documentSnapshot);
    }

    public void setOnItemClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
