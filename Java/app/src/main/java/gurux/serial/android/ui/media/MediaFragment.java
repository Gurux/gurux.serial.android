package gurux.serial.android.ui.media;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import gurux.serial.android.R;
import gurux.serial.android.databinding.FragmentMediaBinding;

public class MediaFragment extends Fragment {

    private FragmentMediaBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MediaViewModel mediaViewModel =
                new ViewModelProvider(requireActivity()).get(MediaViewModel.class);

        binding = FragmentMediaBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        if (mediaViewModel.getMedia() != null) {
            Fragment childFragment = mediaViewModel.getMedia().properties();
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.media_fragment_container, childFragment).commit();
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}