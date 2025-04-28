package gurux.serial.android.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.Locale;
import java.util.Objects;

import gurux.common.GXCommon;
import gurux.common.IGXMediaListener;
import gurux.common.MediaStateEventArgs;
import gurux.common.PropertyChangedEventArgs;
import gurux.common.ReceiveEventArgs;
import gurux.common.TraceEventArgs;
import gurux.common.enums.MediaState;
import gurux.serial.GXSerial;
import gurux.serial.android.R;
import gurux.serial.android.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment implements IGXMediaListener {

    private FragmentHomeBinding binding;
    boolean bHex = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final HomeViewModel homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        enableUI(false);
        final TextView info = binding.info;
        final EditText sendData = binding.sendData;
        final Button openBtn = binding.openBtn;
        final Button clearBtn = binding.clearBtn;
        final Button sendBtn = binding.sendBtn;
        final CheckBox hexCb = binding.hex;
        hexCb.setOnCheckedChangeListener((buttonView, isChecked) -> bHex = isChecked);
        openBtn.setOnClickListener(v -> {
            try {
                final GXSerial serial = homeViewModel.getSerial().getValue();
                if (serial != null) {
                    if (serial.isOpen()) {
                        serial.close();
                    } else {
                        serial.open();
                    }
                }
            } catch (Exception ex) {
                Log.e("Network", ex.getMessage());
                Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        clearBtn.setOnClickListener(v -> binding.receivedData.setText(""));

        sendBtn.setOnClickListener(v -> {
            try {
                final GXSerial serial = homeViewModel.getSerial().getValue();
                if (serial != null) {
                    if (bHex) {
                        serial.send(GXCommon.hexToBytes(sendData.getText().toString()));
                    } else {
                        serial.send(sendData.getText().toString());
                    }
                }
            } catch (Exception ex) {
                Log.e("Network", Objects.requireNonNull(ex.getMessage()));
                Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        homeViewModel.getSerial().observe(getViewLifecycleOwner(), serial -> {
            //Remove old listener.
            serial.removeListener(this);
            serial.addListener(this);
            enableUI(serial.isOpen());
            info.setText(String.format(Locale.getDefault(), "%s %d:%d%s%d", serial.getPort(),
                    serial.getBaudRate().getValue(),serial.getDataBits(), serial.getParity(), 1 + serial.getStopBits().ordinal()));
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onError(Object sender, RuntimeException ex) {

    }

    @Override
    public void onReceived(Object sender, ReceiveEventArgs e) {
        if (bHex) {
            binding.receivedData.append(GXCommon.bytesToHex((byte[]) e.getData()));
        } else {
            binding.receivedData.append(new String((byte[]) e.getData()));
        }
    }

    private void enableUI(boolean open) {
        final Button openBtn = binding.openBtn;
        final Button sendBtn = binding.sendBtn;
        if (open) {
            openBtn.setText(R.string.close);
        } else {
            openBtn.setText(R.string.open);
        }
        sendBtn.setEnabled(open);
    }

    @Override
    public void onMediaStateChange(Object sender, MediaStateEventArgs e) {
        if (e.getState() == MediaState.CLOSING) {
            enableUI(false);

        }
        if (e.getState() == MediaState.OPEN) {
            enableUI(true);
        }
    }

    @Override
    public void onTrace(Object sender, TraceEventArgs e) {

    }

    @Override
    public void onPropertyChanged(Object sender, PropertyChangedEventArgs e) {

    }
}