package eu.mlab.civitas.android.addartefact;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.List;

import eu.mlab.civitas.android.R;
import eu.mlab.civitas.android.model.ArtefactItem;
import eu.mlab.civitas.android.util.BitmapHandler;
import eu.mlab.civitas.android.util.Util;

public class ArtefactItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<ArtefactItem> artefactItems;

    public class ViewHolderArtefactItems extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView textView;
        private ImageButton buttonRemoveItem;

        public ViewHolderArtefactItems(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.image_artefact_item_image);
            textView = (TextView) view.findViewById(R.id.text_artefact_item_description);
            buttonRemoveItem = (ImageButton) view.findViewById(R.id.text_artefact_item_remove);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, CreateArtefactItemActivity.class);
                    intent.putExtra(Util.INTENT_ARTEFACT_ITEM, artefactItems.get(getAdapterPosition()));
                    intent.putExtra(Util.INTENT_ARTEFACT_ITEM_POSITION, getAdapterPosition());
                    intent.putExtra(Util.INTENT_REQUEST_CODE, Util.ARTEFACT_ITEM_UPDATE_REQUEST);
                    ((Activity) context).startActivityForResult(intent, Util.ARTEFACT_ITEM_UPDATE_REQUEST);
                }
            });
        }
    }

    public ArtefactItemAdapter(Context context, List<ArtefactItem> artefactItems) {
        this.context = context;
        this.artefactItems = artefactItems;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolderArtefactItems(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_add_artefact_pictures_cardview, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder item, final int position) {
        ViewHolderArtefactItems viewHolder = (ViewHolderArtefactItems) item;
        if (artefactItems.get(position).getImagePath() == null) {
            return;
        }
        Bitmap bitmap = null;
        File imagePath = new File(context.getFilesDir(), Util.FILE_DIR_IMAGES);
        File newFile = new File(imagePath, artefactItems.get(position).getImagePath());
        try {
            bitmap = BitmapHandler.handleSamplingAndRotationBitmap(context, artefactItems.get(position).getImagePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (bitmap == null) {
            bitmap = BitmapHandler.decodeFile(artefactItems.get(position).getImagePath(), context);
        }

        viewHolder.imageView.setImageBitmap(bitmap);
        String description = artefactItems.get(position).getDescription();
        if (description.length() > 135) {
            description = description.substring(0, 135) + " ...";
        }
        viewHolder.textView.setText(description);
        viewHolder.buttonRemoveItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                artefactItems.remove(position);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return artefactItems.size();
    }
}
