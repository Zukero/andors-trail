package com.gpl.rpg.AndorsTrail.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gpl.rpg.AndorsTrail.AndorsTrailApplication;
import com.gpl.rpg.AndorsTrail.AndorsTrailPreferences;
import com.gpl.rpg.AndorsTrail.R;
import com.gpl.rpg.AndorsTrail.context.ControllerContext;
import com.gpl.rpg.AndorsTrail.model.ModelContainer;
import com.gpl.rpg.AndorsTrail.model.actor.Player;

import java.util.Set;

public final class LoadSavePresetActivity extends Activity implements OnClickListener {
	private boolean isLoading = false;
	private boolean isSaving = false;
	private static final int SLOT_NUMBER_MANUAL_ENTRY = -1;
	private static final int SLOT_NUMBER_FIRST_SLOT = 1;
	private ModelContainer model;
	private Player player;
	private AndorsTrailPreferences preferences;

	private Button  presetEditorButton;
	private EditText presetEditorText;
	private TextView tv;

/*	private ArrayList<String> presetNames;
	private View preset_menu_view;
	private LinearLayout presetMenu;
	private ListView presetList;
	private ArrayAdapter<String> presetListAdapter;*/

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final AndorsTrailApplication app = AndorsTrailApplication.getApplicationFromActivity(this);
		app.setWindowParameters(this);
		this.model = app.getWorld().model;
		this.preferences = app.getPreferences();
		this.player = model.player;

		String loadsave = getIntent().getData().getLastPathSegment();
		isLoading = (loadsave.equalsIgnoreCase("preset_load"));
		isSaving = (loadsave.equalsIgnoreCase("preset_save"));

		setContentView(R.layout.preset_menu);

		tv = (TextView) findViewById(R.id.loadsave_preset_title);

		ViewGroup slotList = (ViewGroup) findViewById(R.id.loadsave_preset_slot_list);
		Button slotTemplateButton = (Button) findViewById(R.id.loadsave_preset_slot_n);
		presetEditorButton = (Button) findViewById(R.id.presets_editor_btn);
		presetEditorText = (EditText) findViewById(R.id.presets_editor_box);
		LayoutParams params = slotTemplateButton.getLayoutParams();
/*		presetNames = new ArrayList<>(player.inventory.presets.keySet());
		presetListAdapter = new ArrayAdapter<String>(getActivity(), R.layout.preset_menu, presetNames);
		presetList.setAdapter(presetListAdapter);*/
		slotList.removeView(slotTemplateButton);

		addPresetSlotButtons(slotList, params, player.inventory.presets.keySet());

		setButtonsVisibility();
	}

	private void setButtonsVisibility() {
		if (isLoading) {
			tv.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_search, 0, 0, 0);
//			presetEditorButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_search, 0, 0, 0);
			tv.setText(R.string.inventory_preset_load);
		} else if (isSaving){
			tv.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_save, 0, 0, 0);
//			presetEditorButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_save, 0, 0, 0);
			tv.setText(R.string.inventory_preset_save);
		} else{
			tv.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_delete, 0, 0, 0);
//			presetEditorButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_delete, 0, 0, 0);
			tv.setText(R.string.inventory_preset_delete);
		}

		if (isSaving ) {
			presetEditorButton.setText(R.string.inventory_preset_save);
			presetEditorButton.setOnClickListener(this);
			presetEditorText.setVisibility(View.VISIBLE);
			presetEditorButton.setVisibility(View.VISIBLE);
			presetEditorButton.setTag(SLOT_NUMBER_MANUAL_ENTRY);
		}
		else{
			presetEditorButton.setVisibility(View.GONE);
			presetEditorText.setVisibility(View.GONE);

			/*if(presetEditorText.getHeight() > presetEditorButton.getHeight())
				presetEditorButton.setHeight(presetEditorText.getHeight() );*/
		}
	}

	private void addPresetSlotButtons(ViewGroup parent, LayoutParams params, Set presets) {
		for (Object slot : presets) {
//			final String header = player.inventory.quickloadPreset(slot.toString(), player);
			//if (header == null) continue;

			final int APcost = player.getPresetEquipCost(slot.toString());
			Button b = new Button(this);
			b.setLayoutParams(params);
			b.setTag(slot);
			b.setOnClickListener(this);
			if(model.uiSelections.isInCombat)
				b.setText(getString(R.string.inventory_preset_select_description, slot.toString(), APcost));// + header);
			else b.setText(slot.toString());

			parent.addView(b, params);
		}
	}

	public void loadsave(String name) {
		/*if (slot == SLOT_NUMBER_MANUAL_ENTRY) {
			List<Integer> usedSlots = Savegames.getUsedSavegameSlots();
			if (usedSlots.isEmpty()) slot = SLOT_NUMBER_FIRST_SLOT;
			else slot = Collections.max(usedSlots) + 1;
		}
		if (slot < SLOT_NUMBER_FIRST_SLOT) slot = SLOT_NUMBER_FIRST_SLOT;*/
		
		Intent i = new Intent();
		//i.putExtra("slot", slot);
		i.putExtra("name", name);
		setResult(Activity.RESULT_OK, i);
//		if(isCombatLoading)
		LoadSavePresetActivity.this.finish();
	}

	private String getConfirmOverwriteQuestion(Object slot) {
		if (isLoading) return null;
		if (slot == SLOT_NUMBER_MANUAL_ENTRY && // if creating a new slot
				!player.inventory.presets.containsKey(slot)) return null;

		if (preferences.displayOverwriteSavegame == AndorsTrailPreferences.CONFIRM_OVERWRITE_SAVEGAME_ALWAYS) {
			return getString(R.string.loadsave_save_overwrite_confirmation_all);
		}
		if (preferences.displayOverwriteSavegame == AndorsTrailPreferences.CONFIRM_OVERWRITE_SAVEGAME_NEVER) {
			return null;
		}

		return getString(R.string.inventory_preset_overwrite_message, slot +"");
	}

	@Override
	public void onClick(View view) {
		 // Don't really need the slot except to check for new creation
		final Object slot = view.getTag();

		final String message = getConfirmOverwriteQuestion(slot);

		if(view.equals(presetEditorButton)){ // i.e. slot == -1
			String name = presetEditorText.getText().toString();
			if(name.equals("")) return;
			loadsave(name.toString());
			return;
		}
		if (message != null) {
			final String title =
				getString(R.string.loadsave_save_overwrite_confirmation_title) + ' ';
				//+ getString(R.string.loadsave_save_overwrite_confirmation_slot, (Integer)slot);
			new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						loadsave(slot.toString());
					}
				})
				.setNegativeButton(android.R.string.no, null)
				.show();
		} else {
			loadsave( slot.toString());
		}
	}
}
