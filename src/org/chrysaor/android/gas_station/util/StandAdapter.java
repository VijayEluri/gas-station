package org.chrysaor.android.gas_station.util;

import java.util.ArrayList;
import java.util.Arrays;

import org.chrysaor.android.gas_station.R;
import org.chrysaor.android.gas_station.activity.DetailActivity;
import org.chrysaor.android.gas_station.lib.database.DatabaseHelper;
import org.chrysaor.android.gas_station.lib.database.FavoritesDao;
import org.chrysaor.android.gas_station.lib.dto.Stand;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class StandAdapter extends ArrayAdapter {

    private ArrayList<Stand> items;
    private LayoutInflater inflater;
    private Context context;
    private Stand item;
    private String[] favList;
    private DatabaseHelper dbHelper = null;
    private SQLiteDatabase db = null;
    private Integer[] favStates;

    public StandAdapter(Context context, int textViewResourceId,
            ArrayList<Stand> items) {
        super(context, textViewResourceId, items);
        this.items = items;
        this.context = context;
        this.inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        favStates = new Integer[items.size()];
        dbHelper = new DatabaseHelper(context);
        updateFavList();

    }

    private void updateFavList() {
        db = dbHelper.getReadableDatabase();
        FavoritesDao favoriteDao = new FavoritesDao(db);
        favList = favoriteDao.getShopCdList();
        db.close();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            // 受け取ったビューがnullなら新しくビューを生成
            convertView = inflater.inflate(R.layout.list_row, null);
            // 背景画像をセットする
            convertView.setBackgroundResource(R.drawable.button_shop);

            holder = new ViewHolder();
            holder.screenName = (TextView) convertView
                    .findViewById(R.id.shop_name);
            holder.text = (TextView) convertView.findViewById(R.id.address);
            holder.price = (TextView) convertView.findViewById(R.id.price);
            holder.dist = (TextView) convertView.findViewById(R.id.distance);
            holder.brand = (ImageView) convertView.findViewById(R.id.icon);
            holder.imgFavorite = (ImageView) convertView
                    .findViewById(R.id.imgFav);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // 表示すべきデータの取得
        item = (Stand) items.get(position);

        if (item != null) {
            // スクリーンネームをビューにセット
            holder.screenName.setTypeface(Typeface.DEFAULT_BOLD);
            if (holder.screenName != null) {
                holder.screenName.setText(item.shopName);
            }

            // テキストをビューにセット
            if (holder.text != null) {
                holder.text.setText(item.address);
            }

            // テキストをビューにセット
            if (holder.price != null) {
                if (item.price.equals("9999")) {
                    holder.price.setText("no data");
                } else {
                    holder.price.setText(item.price + "円");
                }
                holder.price.setTextColor(item.getDispPriceColor());
            }

            // テキストをビューにセット
            if (holder.dist != null) {
                if (item.distance != null) {
                    Float distance = Float.parseFloat(item.distance) / 1000;
                    holder.dist.setText(distance.toString() + "km");
                } else {
                    // 距離が登録されてない（お気に入りGS）の場合、非表示
                    holder.dist.setVisibility(View.GONE);
                }
            }

            StandsHelper helper = StandsHelper.getInstance();
            holder.brand.setImageDrawable(context.getResources().getDrawable(
                    helper.getBrandImage(item.brand,
                            Integer.valueOf(item.price))));

            // お気に入り
            if (favList == null
                    || Arrays.binarySearch(favList, item.shopCode) < 0) {
                holder.imgFavorite.setVisibility(View.INVISIBLE);
            } else {
                holder.imgFavorite.setVisibility(View.VISIBLE);
            }
        }

        return convertView;
    }

    static class ViewHolder {
        TextView screenName;
        TextView text;
        ImageView image;
        TextView price;
        TextView dist;
        ImageView brand;
        ImageView imgFavorite;
    }
}