package org.chrysaor.android.gas_station.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import org.chrysaor.android.gas_station.R;
import org.chrysaor.android.gas_station.lib.database.DatabaseHelper;
import org.chrysaor.android.gas_station.lib.database.FavoritesDao;
import org.chrysaor.android.gas_station.lib.dto.Stand;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressWarnings("rawtypes")
public class StandAdapter<T> extends ArrayAdapter {

    private ArrayList<Stand> items;
    private LayoutInflater inflater;
    private Context context;
    private int resource;
    private String[] favList;
    private DatabaseHelper dbHelper = null;
    private SQLiteDatabase db = null;
    private Integer[] favStates;

    @SuppressWarnings("unchecked")
    public StandAdapter(Context context, int resource, ArrayList<Stand> items) {
        super(context, resource, items);
        this.items = items;
        this.context = context;
        this.resource = resource;
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
            convertView = inflater.inflate(resource, null);

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
        Stand item = (Stand) items.get(position);

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
                    holder.price.setText("---");
                } else {
                    holder.price.setText(item.price + "円");
                }
                holder.price.setTextColor(item.getDispPriceColor());
            }

            // テキストをビューにセット
            if (holder.dist != null) {
                if (item.distance != null) {
                    Float distance = Float.parseFloat(item.distance) / 1000;
                    BigDecimal bi = new BigDecimal(String.valueOf(distance));

                    // 小数第三位で切り上げ
                    double k2 = bi.setScale(2, BigDecimal.ROUND_UP)
                            .doubleValue();
                    holder.dist.setText(k2 + "km");
                } else {
                    // 距離が登録されてない（お気に入りGS）の場合、非表示
                    holder.dist.setVisibility(View.INVISIBLE);
                }
            }

            StandsHelper helper = StandsHelper.getInstance();
            holder.brand.setImageDrawable(context.getResources().getDrawable(
                    helper.getIconImage(item.brand)));

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