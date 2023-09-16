package l2s.gameserver.botscript.bypasshandler;

import l2s.gameserver.botscript.*;
import l2s.gameserver.core.*;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.skills.SkillEntry;

public class BotConfigSet
{
	@Bypass(value = "bot.configSet")
	public void configSet(Player player, NpcInstance npc, String[] param) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
	{
		block132:
		{
			try
			{
				BotConfigImp config;
				block156:
				{
					block173:
					{
						block140:
						{
							block157:
							{
								block180:
								{
									block139:
									{
										block149:
										{
											block148:
											{
												block171:
												{
													block163:
													{
														block168:
														{
															block147:
															{
																block177:
																{
																	block176:
																	{
																		block142:
																		{
																			block167:
																			{
																				block166:
																				{
																					block172:
																					{
																						block146:
																						{
																							block151:
																							{
																								block164:
																								{
																									block165:
																									{
																										block137:
																										{
																											block141:
																											{
																												block143:
																												{
																													block152:
																													{
																														block153:
																														{
																															block175:
																															{
																																block135:
																																{
																																	block161:
																																	{
																																		block136:
																																		{
																																			block160:
																																			{
																																				block150:
																																				{
																																					block158:
																																					{
																																						block181:
																																						{
																																							block170:
																																							{
																																								block169:
																																								{
																																									block162:
																																									{
																																										block144:
																																										{
																																											block134:
																																											{
																																												block154:
																																												{
																																													block174:
																																													{
																																														block133:
																																														{
																																															block159:
																																															{
																																																block145:
																																																{
																																																	block178:
																																																	{
																																																		block179:
																																																		{
																																																			block155:
																																																			{
																																																				block138:
																																																				{
																																																					String fieldName;
																																																					config = (BotConfigImp) BotEngine.getInstance().getBotConfig(player);
																																																					String string = fieldName = param[0];
																																																					switch(string.hashCode())
																																																					{
																																																						case -2117792136:
																																																						{
																																																							if(!string.equals("acceptRes"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block133;
																																																						}
																																																						case -2032811550:
																																																						{
																																																							if(!string.equals("loottype"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block134;
																																																						}
																																																						case -2027246814:
																																																						{
																																																							if(!string.equals("followMove"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block135;
																																																						}
																																																						case -2027107547:
																																																						{
																																																							if(!string.equals("followRest"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block136;
																																																						}
																																																						case -1974210183:
																																																						{
																																																							if(!string.equals("maxDistance"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block137;
																																																						}
																																																						case -1881454641:
																																																						{
																																																							if(!string.equals("pickUpItem"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block138;
																																																						}
																																																						case -1809879639:
																																																						{
																																																							if(!string.equals("petIdleAction"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block139;
																																																						}
																																																						case -1773059242:
																																																						{
																																																							if(!string.equals("limitEvade"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block140;
																																																						}
																																																						case -1751869988:
																																																						{
																																																							if(!string.equals("summonId"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block141;
																																																						}
																																																						case -1749287172:
																																																						{
																																																							if(!string.equals("sprotect"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block142;
																																																						}
																																																						case -1733520978:
																																																						{
																																																							if(!string.equals("hpmpshiftpercent"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block143;
																																																						}
																																																						case -1650946192:
																																																						{
																																																							if(!string.equals("pettarget"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block144;
																																																						}
																																																						case -1274128273:
																																																						{
																																																							if(!string.equals("absorbBody"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block145;
																																																						}
																																																						case -1220273777:
																																																						{
																																																							if(!string.equals("addpoint"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block146;
																																																						}
																																																						case -1188174712:
																																																						{
																																																							if(!string.equals("gprotect"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block147;
																																																						}
																																																						case -1178662002:
																																																						{
																																																							if(!string.equals("itemId"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block148;
																																																						}
																																																						case -1135253496:
																																																						{
																																																							if(!string.equals("keepMp"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block149;
																																																						}
																																																						case -1129450525:
																																																						{
																																																							if(!string.equals("mpHealOrder"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block150;
																																																						}
																																																						case -897943337:
																																																						{
																																																							if(string.equals("autoAttack"))
																																																								break;
																																																							break block132;
																																																						}
																																																						case -717256861:
																																																						{
																																																							if(!string.equals("clearpoint"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block151;
																																																						}
																																																						case -677094926:
																																																						{
																																																							if(!string.equals("petbuff"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block152;
																																																						}
																																																						case -523293047:
																																																						{
																																																							if(!string.equals("summonAttack"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block153;
																																																						}
																																																						case -483891554:
																																																						{
																																																							if(!string.equals("antidote"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block154;
																																																						}
																																																						case -346495695:
																																																						{
																																																							if(!string.equals("coverMember"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block155;
																																																						}
																																																						case -339185956:
																																																						{
																																																							if(!string.equals("balance"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block156;
																																																						}
																																																						case -177454587:
																																																						{
																																																							if(!string.equals("limitDefense"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block157;
																																																						}
																																																						case -162026653:
																																																						{
																																																							if(!string.equals("isUseRes"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block158;
																																																						}
																																																						case -130468201:
																																																						{
																																																							if(!string.equals("hpmpShift"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block159;
																																																						}
																																																						case 3496916:
																																																						{
																																																							if(!string.equals("rest"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block160;
																																																						}
																																																						case 55313864:
																																																						{
																																																							if(!string.equals("idleRest"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block161;
																																																						}
																																																						case 63546204:
																																																						{
																																																							if(!string.equals("bondage"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block162;
																																																						}
																																																						case 95011658:
																																																						{
																																																							if(!string.equals("cubic"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block163;
																																																						}
																																																						case 109399969:
																																																						{
																																																							if(!string.equals("shape"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block164;
																																																						}
																																																						case 402054299:
																																																						{
																																																							if(!string.equals("maxZDiff"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block165;
																																																						}
																																																						case 497381531:
																																																						{
																																																							if(!string.equals("hppotion"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block166;
																																																						}
																																																						case 621498614:
																																																						{
																																																							if(!string.equals("mppotion"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block167;
																																																						}
																																																						case 678752706:
																																																						{
																																																							if(!string.equals("mprotect"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block168;
																																																						}
																																																						case 1042063268:
																																																						{
																																																							if(!string.equals("partyAntidote"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block169;
																																																						}
																																																						case 1082601878:
																																																						{
																																																							if(!string.equals("partyBondage"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block170;
																																																						}
																																																						case 1282388009:
																																																						{
																																																							if(!string.equals("removempp"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block171;
																																																						}
																																																						case 1316782738:
																																																						{
																																																							if(!string.equals("startPos"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block172;
																																																						}
																																																						case 1335347285:
																																																						{
																																																							if(!string.equals("evaPercent"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block173;
																																																						}
																																																						case 1367627622:
																																																						{
																																																							if(!string.equals("usePhysicalAttack"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block174;
																																																						}
																																																						case 1391971673:
																																																						{
																																																							if(!string.equals("followAttack"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block175;
																																																						}
																																																						case 1589540048:
																																																						{
																																																							if(!string.equals("petprotect"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block176;
																																																						}
																																																						case 1612216415:
																																																						{
																																																							if(!string.equals("pprotect"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block177;
																																																						}
																																																						case 1650300541:
																																																						{
																																																							if(!string.equals("autoSweep"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block178;
																																																						}
																																																						case 1801362804:
																																																						{
																																																							if(!string.equals("pickUpFirst"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block179;
																																																						}
																																																						case 1949449594:
																																																						{
																																																							if(!string.equals("followAttackWhenChoosed"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block180;
																																																						}
																																																						case 2014091274:
																																																						{
																																																							if(!string.equals("partyParalysis"))
																																																							{
																																																								break block132;
																																																							}
																																																							break block181;
																																																						}
																																																					}
																																																					config.setAutoAttack(!config.isAutoAttack());
																																																					break block132;
																																																				}
																																																				config.setPickUpItem(!config.isPickUpItem());
																																																				break block132;
																																																			}
																																																			config.setCoverMember(!config.isCoverMember());
																																																			break block132;
																																																		}
																																																		config.setPickUpFirst(!config.isPickUpFirst());
																																																		break block132;
																																																	}
																																																	config.setAutoSweep(!config.isAutoSweep());
																																																	break block132;
																																																}
																																																config.setAbsorbBody(!config.isAbsorbBody());
																																																break block132;
																																															}
																																															config.setHpmpShift(!config.isHpmpShift());
																																															break block132;
																																														}
																																														config.setAcceptRes(!config.isAcceptRes());
																																														break block132;
																																													}
																																													config.setUsePhysicalAttack(!config.isUsePhysicalAttack());
																																													break block132;
																																												}
																																												config.setAntidote(!config.isAntidote());
																																												break block132;
																																											}
																																											config.setLootType(LootType.valueOf(param[1]));
																																											BotControlPage.party(player,param);
																																											return;
																																										}
																																										config.setPetTargetChoose(PetTargetChoose.valueOf(param[1]));
																																										BotControlPage.petPage(player);
																																										return;
																																									}
																																									config.setBondage(!config.isBondage());
																																									break block132;
																																								}
																																								config.setPartyAntidote(!config.isPartyAntidote());
																																								break block132;
																																							}
																																							config.setPartyBondage(!config.isPartyBondage());
																																							break block132;
																																						}
																																						config.setPartyParalysis(!config.isPartyParalysis());
																																						break block132;
																																					}
																																					config.setUseRes(!config.isUseRes());
																																					BotControlPage.protectPage(player);
																																					return;
																																				}
																																				config.setMpHealOrder(MpHealOrder.valueOf(param[1]));
																																				BotControlPage.protectPage(player);
																																				return;
																																			}
																																			config.setHpProtected(Integer.parseInt(param[1]));
																																			config.setMpProtected(Integer.parseInt(param[2]));
																																			BotControlPage.restPage(player);
																																			return;
																																		}
																																		config.setFollowRest(!config.isFollowRest());
																																		BotControlPage.restPage(player);
																																		return;
																																	}
																																	config.setIdleRest(!config.isIdleRest());
																																	BotControlPage.restPage(player);
																																	return;
																																}
																																config.setFollowMove(!config.isFollowMove());
																																break block132;
																															}
																															config.setFollowAttack(!config.isFollowAttack());
																															break block132;
																														}
																														config.setSummonAttack(!config.isSummonAttack());
																														BotControlPage.petPage(player);
																														return;
																													}
																													int petbuffId = Integer.parseInt(param[1]);
																													if(config.getPetBuffs().contains(petbuffId))
																													{
																														config.getPetBuffs().remove(petbuffId);
																													}
																													else
																													{
																														config.getPetBuffs().add(petbuffId);
																													}
																													BotControlPage.petPage(player);
																													return;
																												}
																												String str = param[1];
																												for (char c : str.toCharArray()) {
																													if (!Character.isDigit(c)) {
																														player.sendMessage("请输入0-100之间的数字");
																														player.sendActionFailed();
																														return;
																													}
																												}
																												if (Integer.parseInt(param[1])<0 || Integer.parseInt(param[1])>100) {
																													player.sendMessage("请输入0-100之间的数字");
																													player.sendActionFailed();
																													return;
																												}
																												config.setHpMpShiftPercent(Integer.parseInt(param[1]));
																												break block132;
																											}
																											String skillName = param[1];
																											if("\u65e0".equals(skillName))
																											/*\u65e0 无*/
																											{
																												config.setSummonSkillId(0);
																											}
																											else
																											{
																												for(SkillEntry skillEntry : player.getAllSkillsArray())
																												{
																													if(!skillEntry.getName(player).startsWith(skillName))
																														continue;
																													config.setSummonSkillId(skillEntry.getId());
																													break;
																												}
																												config.setSummonAttack(!config.isSummonAttack());
																											}
																											BotControlPage.petPage(player);
																											return;
																										}
																										try
																										{
																											int distance = Integer.parseInt(param[1]);
																											distance = Math.max(0, distance);
																											distance = Math.min(BotProperties.MAX_SEARCH_RANGE, distance);
																											config.setFindMobMaxDistance(distance);
																										}
																										catch(Exception distance)
																										{
																											// empty catch block
																										}
																										BotControlPage.pathPage(player);
																										return;
																									}
																									try
																									{
																										int zDiff = Integer.parseInt(param[1]);
																										zDiff = Math.max(0, zDiff);
																										zDiff = Math.min(500, zDiff);
																										config.setFindMobMaxHeight(zDiff);
																									}
																									catch(Exception zDiff)
																									{
																										// empty catch block
																									}
																									BotControlPage.pathPage(player);
																									return;
																								}
																								try
																								{
																									Geometry geometry = param[1].equals("\u77e9\u5f62") ? Geometry.SQUARE : (param[1].equals("\u5706\u5f62") ? Geometry.CIRCLE : Geometry.POLYGON);
																									/*\u77e9\u5f62 矩形	\u5706\u5f62 圆形*/
																									config.setGeometry(geometry);
																								}
																								catch(Exception geometry)
																								{
																									// empty catch block
																								}
																								BotControlPage.pathPage(player);
																								return;
																							}
																							config.getPolygon().reset();
																							player.sendMessage("\u6e05\u7a7a\u591a\u8fb9\u5f62\u8282\u70b9");
																							/*\u6e05\u7a7a\u591a\u8fb9\u5f62\u8282\u70b9 清空多边形节点*/
																							BotControlPage.pathPage(player);
																							return;
																						}
																						if(config.getPolygon().npoints >= 16)
																						{
																							player.sendMessage("\u9876\u70b9\u6570\u4ee5\u8fbe\u5230\u6700\u5927\u652f\u6301\u6570\u503c:16");
																							/*\u9876\u70b9\u6570\u4ee5\u8fbe\u5230\u6700\u5927\u652f\u6301\u6570\u503c 顶点数以达到最大支持数值*/
																						}
																						else
																						{
																							config.getPolygon().addPoint(player.getX(), player.getY());
																							int objectId = 1000 + config.getPolygon().npoints;
																							config.getPhantomItem().add(objectId);
																							player.sendPacket((IBroadcastPacket) new DropItemPacket(objectId, 4209, player.getX(), player.getY(), player.getZ(), player.getObjectId()));
																							player.sendMessage("\u6210\u529f\u6dfb\u52a0\u4e86\u65b0\u7684\u9876\u70b9(" + config.getPolygon().npoints + "/16)");
																							/*\u6210\u529f\u6dfb\u52a0\u4e86\u65b0\u7684\u9876\u70b9 成功添加了新的顶点*/
																						}
																						BotControlPage.pathPage(player);
																						return;
																					}
																					try
																					{
																						if(param.length < 4)
																						{
																							config.setStartX(player.getX());
																							config.setStartY(player.getY());
																							config.setStartZ(player.getZ());
																						}
																						else
																						{
																							int x = Integer.parseInt(param[1]);
																							int y = Integer.parseInt(param[2]);
																							int z = Integer.parseInt(param[3]);
																							config.setStartX(x);
																							config.setStartY(y);
																							config.setStartZ(z);
																						}
																					}
																					catch(Exception x)
																					{
																						// empty catch block
																					}
																					BotControlPage.pathPage(player);
																					return;
																				}
																				int percent = Integer.parseInt(param[1]);
																				int potionId = 29031;/*根据286版本修改*/
																				/*原始
																				int potionId = param[2].equals("\u4f53\u529b\u6cbb\u6108\u836f\u6c34") ? 1060 : (param[2].equals("\u7ec8\u6781\u6cbb\u6108\u836f\u6c34") ? 1539 : 1061);
																				*/
																				/*\u4f53\u529b\u6cbb\u6108\u836f\u6c34 体力治愈药水	\u7ec8\u6781\u6cbb\u6108\u836f\u6c34 终极治愈药水*/
																				config.setPotionHpHeal(percent);
																				config.setHpPotionId(potionId);
																				BotControlPage.protectPage(player);
																				return;
																			}
																			int percentm = Integer.parseInt(param[1]);
																			int mpotionId = 70159;/*根据286版本修改*/
																			/*原始
																			int mpotionId = param[2].equals("\u65b0\u624bMP\u6062\u590d\u5242") ? 90310 : (param[2].equals("\u65b0\u624bMP\u6062\u590d\u5242") ? 90415 : 70159);
																			*/
																			/*\u65b0\u624bMP\u6062\u590d\u5242 新手MP恢复剂*/
																			config.setPotionMpHeal(percentm);
																			config.setMpPotionId(mpotionId);
																			BotControlPage.protectPage(player);
																			return;
																		}
																		if(param.length == 3)
																		{
																			int percent1 = Integer.parseInt(param[1]);
																			int skillId = 0;
																			for(SkillEntry entry : player.getAllSkillsArray())
																			{
																				if(!entry.getName(player).equals(param[2]))
																					continue;
																				skillId = entry.getId();
																			}
																			config.setSelfHpHeal(percent1);
																			config.setHealSkill1(skillId);
																		}
																		BotControlPage.protectPage(player);
																		return;
																	}
																	int percent3 = Integer.parseInt(param[1]);
																	int skillId3 = 0;
																	for(SkillEntry entry : player.getAllSkillsArray())
																	{
																		if(!entry.getName(player).equals(param[2]))
																			continue;
																		skillId3 = entry.getId();
																	}
																	config.setPetHpHeal(percent3);
																	config.setHealSkill3(skillId3);
																	BotControlPage.protectPage(player);
																	return;
																}
																int percent11 = Integer.parseInt(param[1]);
																int skillId1 = 0;
																for(SkillEntry entry : player.getAllSkillsArray())
																{
																	if(!entry.getName(player).equals(param[2]))
																		continue;
																	skillId1 = entry.getId();
																}
																config.setPartyHpHeal(percent11);
																config.setHealSkill2(skillId1);
																BotControlPage.protectPage(player);
																return;
															}
															int gount = Integer.parseInt(param[1].substring(0, param[1].length() - 1));
															int percent_g = Integer.parseInt(param[2].substring(0, param[2].length() - 1));
															int skillIdG = 0;
															for(SkillEntry entry : player.getAllSkillsArray())
															{
																if(!entry.getName(player).equals(param[3]))
																	continue;
																skillIdG = entry.getId();
															}
															config.setPartyHealPercent(percent_g);
															config.setPartyHealSize(gount);
															config.setPartyHealSkillId(skillIdG);
															BotControlPage.protectPage(player);
															return;
														}
														if(config.getPartyMpHeal().size() >= 7)
														{
															player.sendMessage("\u4e0d\u80fd\u518d\u8bbe\u7f6e\u66f4\u591a\u6210\u5458\u4e86\uff0c\u8bf7\u5148\u5220\u9664\u65e0\u7528\u7684\u6210\u5458\uff01");
															/*\u4e0d\u80fd\u518d\u8bbe\u7f6e\u66f4\u591a\u6210\u5458\u4e86\uff0c\u8bf7\u5148\u5220\u9664\u65e0\u7528\u7684\u6210\u5458\uff01 不能再设置更多成员了，请先删除无用的成员！*/
														}
														else
														{
															int percent2 = Integer.parseInt(param[1]);
															String charName = param[2];
															Player member = GameObjectsStorage.getPlayer(charName);
															if(member != null && member.isInSameParty(player))
															{
																config.getPartyMpHeal().put(member.getObjectId(), percent2);
															}
														}
														BotControlPage.protectPage(player);
														return;
													}
													int index = Integer.parseInt(param[1]);
													int skillId = Integer.parseInt(param[2]);
													if(skillId == config.getAutoCubic()[index])
													{
														config.setCubic(index, 0);
													}
													else
													{
														config.setCubic(index, skillId);
													}
													BotControlPage.petPage(player);
													return;
												}
												int charId = Integer.parseInt(param[1]);
												config.getPartyMpHeal().remove(charId);
												BotControlPage.protectPage(player);
												return;
											}
											int itemId = Integer.parseInt(param[1]);
											if(config.getAutoItemBuffs().contains(itemId))
											{
												config.getAutoItemBuffs().remove((Object) itemId);
											}
											else
											{
												config.getAutoItemBuffs().add(itemId);
											}
											BotControlPage.itemUsePage(player);
											return;
										}
										int keepPercent = Integer.parseInt(param[1]);
										config.setKeepMp(keepPercent);
										BotControlPage.protectPage(player);
										return;
									}
									config.setBpoidleAction(BotPetOwnerIdleAction.valueOf(param[1]));
									BotControlPage.petPage(player);
									return;
								}
								config.setFollowAttackWhenChoosed(!config.isFollowAttackWhenChoosed());
								break block132;
							}
							config.setLimitDefense(Integer.parseInt(param[1]));
							player.sendMessage("\u8bbe\u7f6e:HP\u4f4e\u4e8e" + config.getLimitDefense() + "%\u65f6\u4f7f\u7528\u6781\u9650\u9632\u5fa1");
							/*\u8bbe\u7f6e:HP\u4f4e\u4e8e 设置:HP低于	\u65f6\u4f7f\u7528\u6781\u9650\u9632\u5fa1 时使用极限防御*/
							BotControlPage.protectPage(player);
							return;
						}
						config.setLimitEvade(Integer.parseInt(param[1]));
						player.sendMessage("\u8bbe\u7f6e:HP\u4f4e\u4e8e" + config.getLimitEvade() + "%\u65f6\u4f7f\u7528\u6781\u9650\u95ea\u907f");
						/*\u8bbe\u7f6e:HP\u4f4e\u4e8e 设置:HP低于	\u65f6\u4f7f\u7528\u6781\u9650\u95ea\u907f 时使用极限闪避*/
						BotControlPage.protectPage(player);
						return;
					}
					config.setEvaPercent(Integer.parseInt(param[1].substring(0, param[1].length() - 1)));
					player.sendMessage("\u8bbe\u7f6e:MP\u4f4e\u4e8e" + config.getEvaPercent() + "%\u65f6\u4f7f\u7528\u4f0a\u5a03\u795d\u798f");
					/*\u8bbe\u7f6e:MP\u4f4e\u4e8e 设置:MP低于	\u65f6\u4f7f\u7528\u4f0a\u5a03\u795d\u798f 时使用伊娃祝福*/
					BotControlPage.protectPage(player);
					return;
				}
				int b_count = Integer.parseInt(param[1].substring(0, param[1].length() - 1));
				int b_percent = Integer.parseInt(param[2].substring(0, param[2].length() - 1));
				config.setBalancePercent(b_percent);
				config.setBalanceSize(b_count);
				BotControlPage.protectPage(player);
				return;
			}
			catch(Exception e)
			{
				BotControlPage.mainPage(player);
				player.sendMessage("\u8bf7\u586b\u5199\u5b8c\u6574\u7684\u53c2\u6570\u518d\u63d0\u4ea4\uff01");
				/*\u8bf7\u586b\u5199\u5b8c\u6574\u7684\u53c2\u6570\u518d\u63d0\u4ea4\uff01 请填写完整的参数再提交！*/
			}
		}
		BotControlPage.mainPage(player);
	}
}