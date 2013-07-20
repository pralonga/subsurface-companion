package org.subsurface.ui;

import java.util.Date;

import org.subsurface.R;
import org.subsurface.controller.DiveController;
import org.subsurface.model.DiveLocationLog;
import org.subsurface.util.DateUtils;
import org.subsurface.util.GpsUtil;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * Fragment for dive details.
 * @author Aurelien PRALONG
 *
 */
public class DiveDetailFragment extends SherlockFragment {

	public static String PARAM_DIVE_ID = "PARAM_DIVE_POSITION";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		DiveLocationLog dive = DiveController.instance.getDiveById(getArguments().getLong(PARAM_DIVE_ID));
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.dive_detail, container, false);
		((TextView) rootView.findViewById(R.id.title)).setText(dive.getName());
		((TextView) rootView.findViewById(R.id.date)).setText(
				DateUtils.initGMT(getString(R.string.date_format_full)).format(new Date(dive.getTimestamp())));
		((TextView) rootView.findViewById(R.id.coordinates)).setText(
				GpsUtil.buildCoordinatesString(getActivity(), dive.getLatitude(), dive.getLongitude()));
		return rootView;
	}
}
