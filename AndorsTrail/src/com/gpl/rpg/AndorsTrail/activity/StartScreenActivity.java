package com.gpl.rpg.AndorsTrail.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.gpl.rpg.AndorsTrail.*;
import com.gpl.rpg.AndorsTrail.controller.Constants;
import com.gpl.rpg.AndorsTrail.resource.tiles.TileManager;
import com.gpl.rpg.AndorsTrail.savegames.Savegames;
import com.gpl.rpg.AndorsTrail.savegames.Savegames.FileHeader;

public final class StartScreenActivity extends Activity {

	private static final int INTENTREQUEST_PREFERENCES = 7;
	public static final int INTENTREQUEST_LOADGAME = 9;

	private boolean hasExistingGame = false;
	private Button startscreen_continue;
	private Button startscreen_newgame;
	private Button startscreen_load;
	private TextView startscreen_currenthero;
	private EditText startscreen_enterheroname;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final AndorsTrailApplication app = AndorsTrailApplication.getApplicationFromActivity(this);
		final Resources res = getResources();
		TileManager tileManager = app.getWorld().tileManager;
		tileManager.setDensity(res);
		updatePreferences(false);
		app.setWindowParameters(this);

		setContentView(R.layout.startscreen);

		TextView tv = (TextView) findViewById(R.id.startscreen_version);
		tv.setText('v' + AndorsTrailApplication.CURRENT_VERSION_DISPLAY);

		startscreen_currenthero = (TextView) findViewById(R.id.startscreen_currenthero);
		startscreen_enterheroname = (EditText) findViewById(R.id.startscreen_enterheroname);
		//startscreen_enterheroname.setImeOptions(EditorInfo.IME_ACTION_DONE);

		startscreen_continue = (Button) findViewById(R.id.startscreen_continue);
		startscreen_continue.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				continueGame(false, Savegames.SLOT_QUICKSAVE, null);
			}
		});

		startscreen_newgame = (Button) findViewById(R.id.startscreen_newgame);
		startscreen_newgame.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (hasExistingGame) {
					comfirmNewGame();
				} else {
					createNewGame();
				}
			}
		});

		Button b = (Button) findViewById(R.id.startscreen_about);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(StartScreenActivity.this, AboutActivity.class));
			}
		});

		b = (Button) findViewById(R.id.startscreen_preferences);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(StartScreenActivity.this, Preferences.class);
				startActivityForResult(intent, INTENTREQUEST_PREFERENCES);
			}
		});

		startscreen_load = (Button) findViewById(R.id.startscreen_load);
		startscreen_load.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Dialogs.showLoad(StartScreenActivity.this);
			}
		});

		TextView development_version = (TextView) findViewById(R.id.startscreen_dev_version);
		if (AndorsTrailApplication.DEVELOPMENT_INCOMPATIBLE_SAVEGAMES) {
			//development_version.setText(R.string.startscreen_incompatible_savegames);
			development_version.setText(R.string.startscreen_ranged_info);
			development_version.setVisibility(View.VISIBLE);
		} else if (!AndorsTrailApplication.IS_RELEASE_VERSION) {
			development_version.setText(R.string.startscreen_non_release_version);
			development_version.setVisibility(View.VISIBLE);
		}
//		if (development_version.getVisibility() == View.VISIBLE) {
//			development_version.setText(development_version.getText() +
//					"\nMax Heap: " + Runtime.getRuntime().maxMemory() / 1024 +
//					"\nTile size: " + (int) (32 * res.getDisplayMetrics().density));
//		}

		app.getWorldSetup().startResourceLoader(res);

		if (AndorsTrailApplication.DEVELOPMENT_FORCE_STARTNEWGAME) {
			if (AndorsTrailApplication.DEVELOPMENT_DEBUGRESOURCES) {
				continueGame(true, 0, "Debug player");
			} else {
				continueGame(true, 0, "Player");
			}
		} else if (AndorsTrailApplication.DEVELOPMENT_FORCE_CONTINUEGAME) {
			continueGame(false, Savegames.SLOT_QUICKSAVE, null);
		}
	}

	private void updatePreferences(boolean alreadyStartedLoadingResources) {
		AndorsTrailApplication app = AndorsTrailApplication.getApplicationFromActivity(this);
		AndorsTrailPreferences preferences = app.getPreferences();
		preferences.read(this);
		if (app.setLocale(this)) {
			if (alreadyStartedLoadingResources) {
				// Changing the locale after having loaded the game requires resources to
				// be re-loaded. Therefore, we just exit here.
				Toast.makeText(this, R.string.change_locale_requires_restart, Toast.LENGTH_LONG).show();
				this.finish();
				return;
			}
		}
		app.getWorld().tileManager.updatePreferences(preferences);
	}

	@Override
	protected void onResume() {
		super.onResume();

		String playerName;
		String displayInfo = null;

		FileHeader header = Savegames.quickload(this, Savegames.SLOT_QUICKSAVE);
		if (header != null && header.playerName != null) {
			playerName = header.playerName;
			displayInfo = header.displayInfo;
		} else {
			// Before fileversion 14 (v0.6.7), quicksave was stored in Shared preferences
			SharedPreferences p = getSharedPreferences("quicksave", MODE_PRIVATE);
			playerName = p.getString("playername", null);
			if (playerName != null) {
				displayInfo = "level " + p.getInt("level", -1);
			}
		}
		hasExistingGame = (playerName != null);
		setButtonState(playerName, displayInfo);

		if (isNewVersion()) {
			Dialogs.showNewVersion(this);
		}

		boolean hasSavegames = !Savegames.getUsedSavegameSlots().isEmpty();
		startscreen_load.setEnabled(hasSavegames);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case INTENTREQUEST_LOADGAME:
			if (resultCode != Activity.RESULT_OK) break;
			final int slot = data.getIntExtra("slot", 1);
			continueGame(false, slot, null);
			break;
		case INTENTREQUEST_PREFERENCES:
			updatePreferences(true);
			break;
		}
	}

	private boolean isNewVersion() {
		final String v = "lastversion";
		SharedPreferences s = getSharedPreferences(Constants.PREFERENCE_MODEL_LASTRUNVERSION, MODE_PRIVATE);
		int lastversion = s.getInt(v, 0);
		if (lastversion >= AndorsTrailApplication.CURRENT_VERSION) return false;
		Editor e = s.edit();
		e.putInt(v, AndorsTrailApplication.CURRENT_VERSION);
		e.commit();
		return true;
	}


	private void setButtonState(final String playerName, final String displayInfo) {
		startscreen_continue.setEnabled(hasExistingGame);
		startscreen_newgame.setEnabled(true);
		if (hasExistingGame) {
			startscreen_currenthero.setText(playerName + ", " + displayInfo);
			startscreen_enterheroname.setText(playerName);
			startscreen_enterheroname.setVisibility(View.GONE);
		} else {
			startscreen_currenthero.setText(R.string.startscreen_enterheroname);
			startscreen_enterheroname.setVisibility(View.VISIBLE);
		}
	}

	private void continueGame(boolean createNewCharacter, int loadFromSlot, String name) {
		final WorldSetup setup = AndorsTrailApplication.getApplicationFromActivity(this).getWorldSetup();
		setup.createNewCharacter = createNewCharacter;
		setup.loadFromSlot = loadFromSlot;
		setup.newHeroName = name;
		startActivity(new Intent(this, LoadingActivity.class));
	}

	private void createNewGame() {
		String name = startscreen_enterheroname.getText().toString().trim();
		if (name == null || name.length() <= 0) {
			Toast.makeText(this, R.string.startscreen_enterheroname, Toast.LENGTH_SHORT).show();
			return;
		}
		continueGame(true, 0, name);
	}

	private void comfirmNewGame() {
		new AlertDialog.Builder(this)
		.setTitle(R.string.startscreen_newgame)
		.setMessage(R.string.startscreen_newgame_confirm)
		.setIcon(android.R.drawable.ic_delete)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//continueGame(true);
				hasExistingGame = false;
				setButtonState(null, null);
			}
		})
		.setNegativeButton(android.R.string.cancel, null)
		.create().show();
	}
}
