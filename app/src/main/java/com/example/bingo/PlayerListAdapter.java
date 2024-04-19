package com.example.bingo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.net.wifi.p2p.WifiP2pDevice;
import java.util.List;

public class PlayerListAdapter extends BaseAdapter {

    private Context context;
    private List<WifiP2pDevice> playerList;

    public PlayerListAdapter(Context context, List<WifiP2pDevice> playerList) {
        this.context = context;
        this.playerList = playerList;
    }

    @Override
    public int getCount() {
        return playerList.size();
    }

    @Override
    public Object getItem(int position) {
        return playerList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.player_item, null);
        }

        // Pobranie referencji do TextView w layoucie wiersza
        TextView playerNameTextView = view.findViewById(R.id.playerNameTextView);

        // Pobranie danych gracza na podstawie pozycji
        WifiP2pDevice player = playerList.get(position);

        // Ustawienie nazwy gracza w TextView
        playerNameTextView.setText(player.deviceName);

        return view;
    }
}

