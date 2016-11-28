package io.realm;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Row;

/**
 * Created by Evgeny on 10.10.2016.
 */
public abstract class RealmAD<T extends RealmModel, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private static final List<Long> EMPTY_LIST = new ArrayList(0);

    protected List ids;
    @Nullable
    private RealmResults<T> adapterData;

    private RealmFieldType animatePrimaryIdType;
    private long animatePrimaryColumnIndex;


    public RealmAD(RealmResults<T> adapterData) {
        this.adapterData = adapterData;
        this.adapterData.addChangeListener(getRealmChangeListener());
        this.animatePrimaryColumnIndex = adapterData.getTable().getTable().getPrimaryKey();
        this.animatePrimaryIdType = adapterData.getTable().getColumnType(this.animatePrimaryColumnIndex);
    }


    @Override
    public int getItemCount() {
        return isDataValid() ? adapterData.size() : 0;
    }

    @Nullable
    public T getItem(int index) {
        //noinspection ConstantConditions
        return isDataValid() ? adapterData.get(index) : null;
    }

    private RealmChangeListener<RealmResults<T>> getRealmChangeListener() {
        return element -> {
            if (RealmAD.this.ids != null && !RealmAD.this.ids.isEmpty()) {
                List newIds = RealmAD.this.getIdsOfRealmResults();
                if (newIds.isEmpty()) {
                    RealmAD.this.ids = newIds;
                    RealmAD.this.notifyDataSetChanged();
                    return;
                }

                Patch patch = DiffUtils.diff(RealmAD.this.ids, newIds);
                List deltas = patch.getDeltas();
                RealmAD.this.ids = newIds;
                if (!deltas.isEmpty()) {
                    Iterator delta2 = deltas.iterator();

                    while (delta2.hasNext()) {
                        Delta delta3 = (Delta) delta2.next();
                        if (delta3.getType() == Delta.TYPE.INSERT) {
                            RealmAD.this.notifyItemRangeInserted(delta3.getRevised().getPosition(), delta3.getRevised().size());
                        } else if (delta3.getType() == Delta.TYPE.DELETE) {
                            RealmAD.this.notifyItemRangeRemoved(delta3.getOriginal().getPosition(), delta3.getOriginal().size());
                        } else {
                            RealmAD.this.notifyItemRangeChanged(delta3.getRevised().getPosition(), delta3.getRevised().size());
                        }
                    }
                }
            } else {
                RealmAD.this.notifyDataSetChanged();
                RealmAD.this.ids = RealmAD.this.getIdsOfRealmResults();
            }

        };
    }

    private List getIdsOfRealmResults() {
        if (this.adapterData != null && this.adapterData.size() != 0) {
            ArrayList ids;
            int i;

            ids = new ArrayList(this.adapterData.size());

            for (i = 0; i < this.adapterData.size(); ++i) {
                ids.add(this.getRealmRowId(i));
            }

            return ids;
        } else {
            return EMPTY_LIST;
        }
    }

    private Object getRealmRowId(int realmIndex) {
        RealmObjectProxy proxy = (RealmObjectProxy) this.adapterData.get(realmIndex);
        Row row = proxy.realmGet$proxyState().getRow$realm();
        Object rowPrimaryId;

        animatePrimaryColumnIndex = adapterData.getTable().getTable().getPrimaryKey();
        if (this.animatePrimaryIdType == RealmFieldType.INTEGER) {
            rowPrimaryId = row.getLong(this.animatePrimaryColumnIndex);
        } else {
            if (this.animatePrimaryIdType != RealmFieldType.STRING) {
                throw new IllegalStateException("Unknown animatedIdType");
            }

            rowPrimaryId = row.getString(this.animatePrimaryColumnIndex);
        }
        return rowPrimaryId;
    }

    public Object getLastItem() {
        if (getData() != null && getData().size() != 0) {
            return getData().last();
        }
        return null;
    }

    private boolean isDataValid() {
        return adapterData != null && adapterData.isValid();
    }

    public OrderedRealmCollection<T> getData() {
        return adapterData;
    }

    public int getPositionById(String id) {
        if (ids != null) {
            return ids.indexOf(id);
        } else {
            return 0;
        }

    }
}
