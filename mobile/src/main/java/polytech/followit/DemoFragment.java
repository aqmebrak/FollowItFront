package polytech.followit;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import polytech.followit.model.Instruction;

/**
 * Created by mperrin on 01/02/2017.
 */

public class DemoFragment extends Fragment {
    private static final String ARG_DATA = "data";

    private Instruction mData;
    private OnFragmentInteractionListener mListener;

    public DemoFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance(Instruction instruction) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_DATA, instruction);

        Fragment f = new DemoFragment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.navigation_activity, container, false);
        TextView instructionText = (TextView) v.findViewById(R.id.instructions_textView);

        if(mData != null) {
            instructionText.setText(mData.instruction);

            if(mListener != null) {
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
        // TODO: Update argument type and name
        void onFragmentInteraction(int currentDataPosition);
        void onFragmentCreated(DemoFragment demoFragment);
        void onFragmentResumed(DemoFragment demoFragment);
    }
}