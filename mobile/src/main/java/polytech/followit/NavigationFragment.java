package polytech.followit;

import android.content.Context;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import polytech.followit.adapter.MyCustomDiscountListAdapter;
import polytech.followit.model.Instruction;
import polytech.followit.utility.PathSingleton;


/**
 * Classe représentant une vue du Pager
 */
public class NavigationFragment extends Fragment {
    private static final String ARG_DATA = "data";

    //La classe qui contient les données à afficher
    private Instruction mData;
    //L'adapter pour afficher le contenu de la liste
    MyCustomDiscountListAdapter dataAdapter = null;

    private OnFragmentInteractionListener mListener;

    public NavigationFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance(Instruction instruction) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_DATA, instruction);

        Fragment f = new NavigationFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mData = (Instruction) getArguments().getParcelable(ARG_DATA);
        }
    }

    /**
     * Construit le contenu du pager
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.navigation_activity, container, false);

        //text des instructions
        TextView instructionText = (TextView) v.findViewById(R.id.instructions_textView);

        //construction de la liste
        dataAdapter = new MyCustomDiscountListAdapter(getContext(), R.layout.poi_info, mData.discountList);
        ListView listView = (ListView) v.findViewById(R.id.discount_list);
        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);

        ImageView orientation = (ImageView) v.findViewById(R.id.orientation);
        if (mData.getOrientationIcon() != -1) {
            orientation.setImageDrawable(ContextCompat.getDrawable(getContext(), mData.getOrientationIcon()));
        } else {
            ((ViewGroup) orientation.getParent()).removeView(orientation);
        }

        if (mData != null) {
            instructionText.setText(mData.instruction);

            if (mListener != null) {
                mListener.onFragmentCreated(this);
            }
        }
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mListener.onFragmentResumed(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public Instruction getData() {
        return mData;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(int currentDataPosition);

        void onFragmentCreated(NavigationFragment navigationFragment);

        void onFragmentResumed(NavigationFragment navigationFragment);
    }
}