package stormy.com.stormy;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

public class AlertDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        Context context = getActivity(); // getActvity is a context
        AlertDialog.Builder builder = new AlertDialog.Builder(context) // alert box interface
                .setTitle(R.string.error_title) //Oops! Sorry! (all messages are stored in res/values/strings.xml)
                .setMessage(R.string.error_message) //There was an error. Please try again
                .setPositiveButton(R.string.error_ok_button_text, null); //OK

        AlertDialog dialog = builder.create();
        return dialog;




    }


}
