package com.example.owlagenda.ui.calendar;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.owlagenda.R;

public class TaskViewHolder extends RecyclerView.ViewHolder {
    private TextView itemFlightDateText;
    private TextView itemDepartureAirportCodeText;
    private TextView itemDepartureAirportCityText;
    private TextView itemDestinationAirportCodeText;
    private TextView itemDestinationAirportCityText;

    public TaskViewHolder(@NonNull View itemView) {
        super(itemView);
        itemFlightDateText = itemView.findViewById(R.id.itemFlightDateText);
        itemDepartureAirportCodeText = itemView.findViewById(R.id.itemDepartureAirportCodeText);
        itemDepartureAirportCityText = itemView.findViewById(R.id.itemDepartureAirportCityText);
        itemDestinationAirportCodeText = itemView.findViewById(R.id.itemDestinationAirportCodeText);
        itemDestinationAirportCityText = itemView.findViewById(R.id.itemDestinationAirportCityText);
    }

    public void bind(Task task) {
        itemFlightDateText.setText(Task.flightDateTimeFormatter(task.getTime()));
        itemFlightDateText.setTextColor(itemView.getContext().getColor(task.getColor()));

        itemDepartureAirportCodeText.setText(task.getDeparture().getCode());
        itemDepartureAirportCityText.setText(task.getDeparture().getCity());

        itemDestinationAirportCodeText.setText(task.getDestination().getCode());
        itemDestinationAirportCityText.setText(task.getDestination().getCity());
    }
}
