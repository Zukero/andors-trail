var ATEditor = (function(ATEditor, model, FieldList, _) {

	function addLegacyFieldLists(model) {
		model.actorConditions.legacyFieldList = new FieldList("[id|name|iconID|category|isStacking|isPositive|"
				+ "hasRoundEffect|round_visualEffectID|round_boostHP_Min|round_boostHP_Max|round_boostAP_Min|round_boostAP_Max|"
				+ "hasFullRoundEffect|fullround_visualEffectID|fullround_boostHP_Min|fullround_boostHP_Max|fullround_boostAP_Min|fullround_boostAP_Max|"
				+ "hasAbilityEffect|boostMaxHP|boostMaxAP|moveCostPenalty|attackCost|attackChance|criticalChance|criticalMultiplier|attackDamage_Min|attackDamage_Max|blockChance|damageResistance|"
				+ "];"
			);
		model.quests.legacyFieldList = new FieldList("[id|name|showInLog|stages[progress|logText|rewardExperience|removeQuestProgress|finishesQuest|]|];");
		model.items.legacyFieldList = new FieldList("[id|iconID|name|category|displaytype|hasManualPrice|baseMarketCost|"
				+ "hasEquipEffect|equip_boostMaxHP|equip_boostMaxAP|equip_moveCostPenalty|equip_attackCost|equip_attackChance|equip_criticalChance|equip_criticalMultiplier|equip_attackDamage_Min|equip_attackDamage_Max|equip_blockChance|equip_damageResistance|equip_conditions[condition|magnitude|]|"
				+ "hasUseEffect|use_boostHP_Min|use_boostHP_Max|use_boostAP_Min|use_boostAP_Max|use_conditionsSource[condition|magnitude|duration|chance|]|"
				+ "hasHitEffect|hit_boostHP_Min|hit_boostHP_Max|hit_boostAP_Min|hit_boostAP_Max|hit_conditionsSource[condition|magnitude|duration|chance|]|hit_conditionsTarget[condition|magnitude|duration|chance|]|"
				+ "hasKillEffect|kill_boostHP_Min|kill_boostHP_Max|kill_boostAP_Min|kill_boostAP_Max|kill_conditionsSource[condition|magnitude|duration|chance|]|"
				+ "];"
			);
		model.droplists.legacyFieldList = new FieldList("[id|items[itemID|quantity_Min|quantity_Max|chance|]|];");
		model.dialogue.legacyFieldList = new FieldList("[id|message|rewards[rewardType|rewardID|value|]|replies[text|nextPhraseID|requires_Progress|requires_itemID|requires_Quantity|requires_Type|]|];");
		model.monsters.legacyFieldList = new FieldList("[id|iconID|name|tags|size|monsterClass|unique|faction|maxHP|maxAP|moveCost|attackCost|attackChance|criticalChance|criticalMultiplier|attackDamage_Min|attackDamage_Max|blockChance|damageResistance|droplistID|phraseID|"
				+ "hasHitEffect|onHit_boostHP_Min|onHit_boostHP_Max|onHit_boostAP_Min|onHit_boostAP_Max|onHit_conditionsSource[condition|magnitude|duration|chance|]|onHit_conditionsTarget[condition|magnitude|duration|chance|]|"
				+ "];"
			);
		model.itemCategories.legacyFieldList = new FieldList("[id|name|actionType|inventorySlot|size|];");
	}
	addLegacyFieldLists(model);
	
	ATEditor.legacy = ATEditor.legacy || {};
	ATEditor.legacy.convertFromLegacyFormat = {
		monster: convertMonster
		,quest: convertQuest
		,itemcategory: convertItemCategory
		,item: convertItem
		,droplist: convertDroplist
		,dialogue: convertConversation
		,actorcondition: convertCondition
	};
	
	function convertMonster(obj) {
		// [id|iconID|name|tags|size|monsterClass|unique|faction|maxHP|maxAP|moveCost|attackCost|attackChance|criticalChance|criticalMultiplier|attackDamage_Min|attackDamage_Max|blockChance|
		// damageResistance|droplistID|phraseID|
		// hasHitEffect|onHit_boostHP_Min|onHit_boostHP_Max|onHit_boostAP_Min|onHit_boostAP_Max|onHit_conditionsSource[condition|magnitude|duration|chance|]|onHit_conditionsTarget[condition|magnitude|duration|chance|]|];

		var result = {
			id: obj.id,
			iconID: obj.iconID,
			name: obj.name,
			spawnGroup: obj.tags,
			size: obj.size,
			monsterClass: obj.monsterClass,
			unique: obj.unique,
			faction: obj.faction,
			maxHP: obj.maxHP,
			maxAP: obj.maxAP,
			moveCost: obj.moveCost,
			attackCost: obj.attackCost,
			attackChance: obj.attackChance,
			criticalSkill: obj.criticalChance,
			criticalMultiplier: obj.criticalMultiplier,
			blockChance: obj.blockChance,
			damageResistance: obj.damageResistance,
			droplistID: obj.droplistID,
			phraseID: obj.phraseID
		};

		if (obj.attackDamage_Min || obj.attackDamage_Max) {
			result.attackDamage = { 
				min: (obj.attackDamage_Min || 0), 
				max: (obj.attackDamage_Max || 0)
			};
		}
		
		if (obj.hasHitEffect) {
			result.hitEffect = {};
			var e = result.hitEffect;
			if (obj.onHit_boostHP_Min || obj.onHit_boostHP_Max) { 
				e.increaseCurrentHP = { 
					min: (obj.onHit_boostHP_Min || 0), 
					max: (obj.onHit_boostHP_Max || 0) 
				};
			}
			if (obj.onHit_boostAP_Min || obj.onHit_boostAP_Max) { 
				e.increaseCurrentAP = { 
					min: (obj.onHit_boostAP_Min || 0), 
					max: (obj.onHit_boostAP_Max || 0) 
				};
			}
			if (obj.onHit_conditionsSource) { e.conditionsSource = obj.onHit_conditionsSource; }
			if (obj.onHit_conditionsTarget) { e.conditionsTarget = obj.onHit_conditionsTarget; }
		}

		return result;
	}
	
	
	function convertQuest(obj) {
		// [id|name|showInLog|stages[progress|logText|rewardExperience|removeQuestProgress|finishesQuest|]|];
		return obj;
	}
	
	function convertItemCategory(obj) {
		// [id|name|actionType|inventorySlot|size|];
		return obj;
	}

	function convertItem(obj) {
		//[id|iconID|name|category|displaytype|hasManualPrice|baseMarketCost|
		// hasEquipEffect|equip_boostMaxHP|equip_boostMaxAP|equip_moveCostPenalty|
		//   equip_attackCost|equip_attackChance|equip_criticalChance|equip_criticalMultiplier|equip_attackDamage_Min|equip_attackDamage_Max|equip_blockChance|equip_damageResistance|
		//   equip_conditions[condition|magnitude|]|
		// hasUseEffect|use_boostHP_Min|use_boostHP_Max|use_boostAP_Min|use_boostAP_Max|use_conditionsSource[condition|magnitude|duration|chance|]|
		// hasHitEffect|hit_boostHP_Min|hit_boostHP_Max|hit_boostAP_Min|hit_boostAP_Max|hit_conditionsSource[condition|magnitude|duration|chance|]|hit_conditionsTarget[condition|magnitude|duration|chance|]|
		// hasKillEffect|kill_boostHP_Min|kill_boostHP_Max|kill_boostAP_Min|kill_boostAP_Max|kill_conditionsSource[condition|magnitude|duration|chance|]|];
		
		var result = {
			id: obj.id,
			iconID: obj.iconID,
			name: obj.name,
			category: obj.category,
			displaytype: obj.displaytype,
			hasManualPrice: obj.hasManualPrice,
			baseMarketCost: obj.baseMarketCost
		};
		
		if (obj.hasEquipEffect) {
			result.equipEffect = {};
			var e = result.equipEffect;
			if (obj.equip_boostMaxHP) { e.increaseMaxHP = obj.equip_boostMaxHP; }
			if (obj.equip_boostMaxAP) { e.increaseMaxAP = obj.equip_boostMaxAP; }
			if (obj.equip_moveCostPenalty) { e.increaseMoveCost = obj.equip_moveCostPenalty; }
			if (obj.equip_attackCost) { e.increaseAttackCost = obj.equip_attackCost; }
			if (obj.equip_attackChance) { e.increaseAttackChance = obj.equip_attackChance; }
			if (obj.equip_criticalChance) { e.increaseCriticalSkill = obj.equip_criticalChance; }
			if (obj.equip_criticalMultiplier) { e.setCriticalMultiplier = obj.equip_criticalMultiplier; }
			if (obj.equip_attackDamage_Min || obj.equip_attackDamage_Max) { 
				e.increaseAttackDamage = { 
					min: (obj.equip_attackDamage_Min || 0), 
					max: (obj.equip_attackDamage_Max || 0) 
				};
			}
			if (obj.equip_blockChance) { e.increaseBlockChance = obj.equip_blockChance; }
			if (obj.equip_damageResistance) { e.increaseDamageResistance = obj.equip_damageResistance; }
		}
		
		if (obj.equip_conditions) {
			result.equipEffect = result.equipEffect || {};
			result.equipEffect.addedConditions = obj.equip_conditions;
		}
		
		if (obj.hasUseEffect) {
			result.useEffect = {};
			var e = result.useEffect;
			if (obj.use_boostHP_Min || obj.use_boostHP_Max) { 
				e.increaseCurrentHP = { 
					min: (obj.use_boostHP_Min || 0), 
					max: (obj.use_boostHP_Max || 0) 
				};
			}
			if (obj.use_boostAP_Min || obj.use_boostAP_Max) { 
				e.increaseCurrentAP = { 
					min: (obj.use_boostAP_Min || 0), 
					max: (obj.use_boostAP_Max || 0) 
				};
			}
			if (obj.use_conditionsSource) { e.conditionsSource = obj.use_conditionsSource; }
		}
		
		if (obj.hasHitEffect) {
			result.hitEffect = {};
			var e = result.hitEffect;
			if (obj.hit_boostHP_Min || obj.hit_boostHP_Max) { 
				e.increaseCurrentHP = { 
					min: (obj.hit_boostHP_Min || 0), 
					max: (obj.hit_boostHP_Max || 0) 
				};
			}
			if (obj.hit_boostAP_Min || obj.hit_boostAP_Max) { 
				e.increaseCurrentAP = { 
					min: (obj.hit_boostAP_Min || 0), 
					max: (obj.hit_boostAP_Max || 0) 
				};
			}
			if (obj.hit_conditionsSource) { e.conditionsSource = obj.hit_conditionsSource; }
			if (obj.hit_conditionsTarget) { e.conditionsTarget = obj.hit_conditionsTarget; }
		}
		
		if (obj.hasKillEffect) {
			result.killEffect = {};
			var e = result.killEffect;
			if (obj.kill_boostHP_Min || obj.kill_boostHP_Max) { 
				e.increaseCurrentHP = { 
					min: (obj.kill_boostHP_Min || 0), 
					max: (obj.kill_boostHP_Max || 0) 
				};
			}
			if (obj.kill_boostAP_Min || obj.kill_boostAP_Max) { 
				e.increaseCurrentAP = { 
					min: (obj.kill_boostAP_Min || 0), 
					max: (obj.kill_boostAP_Max || 0) 
				};
			}
			if (obj.kill_conditionsSource) { e.conditionsSource = obj.kill_conditionsSource; }
		}
		
		return result;
	}

	function convertDroplist(obj) {
		// [id|items[itemID|quantity_Min|quantity_Max|chance|]|];
		var result = {
			id: obj.id,
			items: _.map(obj.items, function(obj) {
				return {
					itemID: obj.itemID,
					quantity: { 
						min: (obj.quantity_Min || 0), 
						max: (obj.quantity_Max || 0)
					},
					chance: obj.chance
				};
			})
		};
		
		return result;
	}

	function convertConversation(obj) {
		// [id|message|rewards[rewardType|rewardID|value|]|replies[text|nextPhraseID|requires_Progress|requires_itemID|requires_Quantity|requires_Type|]|];

		var result = {
			id: obj.id,
			message: obj.message,
			rewards: obj.rewards
		};
		if (obj.replies) {
			result.replies = _.map(obj.replies, function(obj) {
				var result = {
					text: obj.text,
					nextPhraseID: obj.nextPhraseID
				};
				
				if (obj.requires_Progress) { 
					result.requires = [];
					result.requires.push({
						requireType: 'questProgress',
						requireID: obj.requires_Progress
					});
				}
				if (obj.requires_itemID) { 
					result.requires = result.requires || []; 
					result.requires.push({ 
						requireType: 1 + obj.requires_Type, 
						requireID: obj.requires_itemID,
						value: obj.requires_Quantity
					});
				}
				
				return result;
			});
		}
		
		return result;
	}

	function convertCondition(obj) {
		// [id|name|iconID|category|isStacking|isPositive|
		// hasRoundEffect|round_visualEffectID|round_boostHP_Min|round_boostHP_Max|round_boostAP_Min|round_boostAP_Max|
		// hasFullRoundEffect|fullround_visualEffectID|fullround_boostHP_Min|fullround_boostHP_Max|fullround_boostAP_Min|fullround_boostAP_Max|
		// hasAbilityEffect|boostMaxHP|boostMaxAP|moveCostPenalty|attackCost|attackChance|criticalChance|criticalMultiplier|attackDamage_Min|attackDamage_Max|blockChance|damageResistance|];

		var result = {
			id: obj.id,
			iconID: obj.iconID,
			name: obj.name,
			category: obj.category,
			isStacking: obj.isStacking,
			isPositive: obj.isPositive
		};
		
		if (obj.hasRoundEffect) {
			result.roundEffect = {};
			var e = result.roundEffect;
			if (obj.round_visualEffectID || obj.round_visualEffectID === 0) { e.visualEffectID = obj.round_visualEffectID; }
			if (obj.round_boostHP_Min || obj.round_boostHP_Max) { 
				e.increaseCurrentHP = { 
					min: (obj.round_boostHP_Min || 0), 
					max: (obj.round_boostHP_Max || 0) 
				};
			}
			if (obj.round_boostAP_Min || obj.round_boostAP_Max) { 
				e.increaseCurrentAP = { 
					min: (obj.round_boostAP_Min || 0), 
					max: (obj.round_boostAP_Max || 0) 
				};
			}
		}
		
		if (obj.hasFullRoundEffect) {
			result.fullRoundEffect = {};
			var e = result.fullRoundEffect;
			if (obj.fullround_visualEffectID || obj.fullround_visualEffectID === 0) { e.visualEffectID = obj.fullround_visualEffectID; }
			if (obj.fullround_boostHP_Min || obj.fullround_boostHP_Max) { 
				e.increaseCurrentHP = { 
					min: (obj.fullround_boostHP_Min || 0), 
					max: (obj.fullround_boostHP_Max || 0) 
				};
			}
			if (obj.fullround_boostAP_Min || obj.fullround_boostAP_Max) { 
				e.increaseCurrentAP = { 
					min: (obj.fullround_boostAP_Min || 0), 
					max: (obj.fullround_boostAP_Max || 0) 
				};
			}
		}

		if (obj.hasAbilityEffect) {
			result.abilityEffect = {};
			var e = result.abilityEffect;
			if (obj.boostMaxHP) { e.increaseMaxHP = obj.boostMaxHP; }
			if (obj.boostMaxAP) { e.increaseMaxAP = obj.boostMaxAP; }
			if (obj.moveCostPenalty) { e.increaseMoveCost = obj.moveCostPenalty; }
			if (obj.attackCost) { e.increaseAttackCost = obj.attackCost; }
			if (obj.attackChance) { e.increaseAttackChance = obj.attackChance; }
			if (obj.criticalChance) { e.increaseCriticalSkill = obj.criticalChance; }
			if (obj.criticalMultiplier) { e.setCriticalMultiplier = obj.criticalMultiplier; }
			if (obj.attackDamage_Min || obj.attackDamage_Max) { 
				e.increaseAttackDamage = { 
					min: (obj.attackDamage_Min || 0), 
					max: (obj.attackDamage_Max || 0) 
				};
			}
			if (obj.blockChance) { e.increaseBlockChance = obj.blockChance; }
			if (obj.damageResistance) { e.increaseDamageResistance = obj.damageResistance; }
		}
		
		return result;
	}

	
	return ATEditor;	
})(ATEditor, ATEditor.model, ATEditor.FieldList, _);
