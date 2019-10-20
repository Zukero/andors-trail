package com.gpl.rpg.AndorsTrail.view;

import android.content.Context;
import android.content.res.Resources;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gpl.rpg.AndorsTrail.AndorsTrailApplication;
import com.gpl.rpg.AndorsTrail.Dialogs;
import com.gpl.rpg.AndorsTrail.R;
import com.gpl.rpg.AndorsTrail.context.WorldContext;
import com.gpl.rpg.AndorsTrail.model.ability.ActorCondition;
import com.gpl.rpg.AndorsTrail.model.ability.ActorConditionEffect;
import com.gpl.rpg.AndorsTrail.model.ability.ActorConditionType;
import com.gpl.rpg.AndorsTrail.model.ability.effectivecondition.EffectiveActorCondition;
import com.gpl.rpg.AndorsTrail.model.ability.effectivecondition.EffectiveActorConditionEffect;
import com.gpl.rpg.AndorsTrail.util.ConstRange;

public final class ActorConditionList extends LinearLayout {

	private final WorldContext world;

	public ActorConditionList(Context context, AttributeSet attr) {
		super(context, attr);
		setFocusable(false);
		setOrientation(LinearLayout.VERTICAL);
		AndorsTrailApplication app = AndorsTrailApplication.getApplicationFromActivityContext(context);
		this.world = app.getWorld();
	}

	public void update(Iterable<EffectiveActorCondition> effectiveConditions, Iterable<ActorCondition> immunities) {
		removeAllViews();

		final Context context = getContext();
		final Resources res = getResources();
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

		for (EffectiveActorCondition c : effectiveConditions) {
			addConditionEffect(context, res, layoutParams, c);
		}

		for (ActorCondition c : immunities) {
			addConditionEffect(context, res, layoutParams, c, true);
		}
	}

	private void addConditionEffect(final Context context, final Resources res, LinearLayout.LayoutParams layoutParams,
									EffectiveActorCondition c) {
		TextView v = (TextView) View.inflate(context, R.layout.actorconditionitemview, null);
		world.tileManager.setImageViewTile(res, v, c.conditionType, false);

		StringBuilder sb = new StringBuilder(60);
		sb.append(describeEffect(res, c));
		int sources = c.appliedEffectsCount();
		if (sources > 1) {
			sb.append(res.getString(R.string.actorcondition_sources, sources));
		}

		int underineLength = sb.length();
		EffectiveActorCondition following = c.calculateFollowingCondition();
		while (following != null){
			sb.append("\n");
			sb.append(describeEffect(res, following));
			following = following.calculateFollowingCondition();
		}

		SpannableString content = new SpannableString(sb);
		content.setSpan(new UnderlineSpan(), 0, underineLength, 0);
		addListElement(context, res, layoutParams, c.conditionType, false, content);
	}

	private void addConditionEffect(final Context context, final Resources res, LinearLayout.LayoutParams layoutParams,
			ActorCondition c, boolean immunity) {
		String description = describeEffect(res, c);
		int underineLength = description.length();
		SpannableString content = new SpannableString(description);
		content.setSpan(new UnderlineSpan(), 0, underineLength, 0);
		addListElement(context, res, layoutParams, c.conditionType, immunity, content);
	}

	private void addListElement(final Context context, final Resources res, LinearLayout.LayoutParams layoutParams,
									ActorConditionType conditionType, boolean immunity, SpannableString content) {
		TextView v = (TextView) View.inflate(context, R.layout.actorconditionitemview, null);
		world.tileManager.setImageViewTile(res, v, conditionType, immunity);

		v.setText(content);
		v.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Dialogs.showActorConditionInfo(context, conditionType);
			}
		});
		this.addView(v, layoutParams);
	}

	private static final ConstRange MAX_CHANCE = new ConstRange(1,1);

	private static String describeEffect(Resources res, EffectiveActorCondition c) {
		return ActorConditionEffectList.describeEffect(res, new ActorConditionEffect(c.conditionType, c.getMagnitude(), c.getDuration(), MAX_CHANCE));
	}

	private static String describeEffect(Resources res, ActorCondition c) {
		return ActorConditionEffectList.describeEffect(res, new ActorConditionEffect(c.conditionType, c.magnitude, c.duration, MAX_CHANCE));
	}
}
