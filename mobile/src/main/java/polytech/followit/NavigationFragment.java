package polytech.followit;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import polytech.followit.adapter.RecyclerViewAdapter;
import polytech.followit.model.Instruction;
import polytech.followit.utility.PathSingleton;


/**
 * Classe représentant une vue du Pager
 */
public class NavigationFragment extends Fragment {
    private static final String ARG_DATA = "data";
    private final String TAG = NavigationFragment.class.getSimpleName();

    //La classe qui contient les données à afficher
    private Instruction mData;
    //L'adapter pour afficher le contenu de la liste
    RecyclerViewAdapter dataAdapter = null;

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
            mData = getArguments().getParcelable(ARG_DATA);
            Log.d(TAG, "OnCreate - Navigation fragment : " + mData);
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

        RecyclerViewAdapter adapter = new RecyclerViewAdapter(mData.discountList);
        RecyclerView myView = (RecyclerView) v.findViewById(R.id.discount_list);
        myView.setHasFixedSize(true);
        myView.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        myView.setLayoutManager(llm);

        ImageView orientation = (ImageView) v.findViewById(R.id.orientation);
        Drawable icon;
        icon = ContextCompat.getDrawable(getContext(), PathSingleton.determineOrientationIcon(mData.getOrientation()));
        orientation.setImageDrawable(icon);

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