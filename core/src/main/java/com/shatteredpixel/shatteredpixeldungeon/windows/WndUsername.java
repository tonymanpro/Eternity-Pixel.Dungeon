/*
 * Eternity Pixel Dungeon
 * Unique Username & Multi-Device Account Management Window
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.services.UsernameService;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

public class WndUsername extends Window {

	protected static final int WIDTH_P = 140;
	protected static final int WIDTH_L = 220;
	protected static final int GAP = 4;

	private Runnable onDismissCallback;

	public WndUsername() {
		this(null);
	}

	public WndUsername(Runnable onDismissCallback) {
		super();
		this.onDismissCallback = onDismissCallback;

		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		IconTitle title = new IconTitle(Icons.get(Icons.RANKINGS), Messages.get(this, "title"));
		title.setRect(0, 0, width, 0);
		add(title);

		String currentUsername = SPDSettings.customUsername();
		boolean hasUsername = currentUsername != null && !currentUsername.trim().isEmpty();
		String currentKey = SPDSettings.accountKey();

		StringBuilder info = new StringBuilder();
		if (hasUsername) {
			info.append(Messages.get(this, "current_username", currentUsername)).append("\n\n");
			if (currentKey != null && !currentKey.isEmpty()) {
				info.append(Messages.get(this, "current_key", currentKey)).append("\n\n");
			}
			info.append(Messages.get(this, "desc_registered"));
		} else {
			info.append(Messages.get(this, "unregistered_notice")).append("\n\n");
			info.append(Messages.get(this, "desc_unregistered"));
		}

		RenderedTextBlock text = PixelScene.renderTextBlock(6);
		text.text(info.toString(), width);
		text.setPos(0, title.bottom() + GAP);
		add(text);

		float pos = text.bottom() + GAP * 2;

		if (hasUsername && currentKey != null && !currentKey.isEmpty()) {
			RedButton btnCopyKey = new RedButton(Messages.get(this, "btn_copy_key")) {
				@Override
				public void onClick() {
					try {
						Gdx.app.getClipboard().setContents(currentKey);
						ShatteredPixelDungeon.scene().addToFront(new WndMessage(Messages.get(WndUsername.class, "key_copied")));
					} catch (Exception ignored) {}
				}
			};
			btnCopyKey.icon(Icons.get(Icons.COPY));
			btnCopyKey.setRect(0, pos, width, 18);
			add(btnCopyKey);
			pos = btnCopyKey.bottom() + GAP;
		}

		// Button to register a new unique username
		String registerBtnLabel = hasUsername ? Messages.get(this, "btn_change_username") : Messages.get(this, "btn_register_username");
		RedButton btnRegister = new RedButton(registerBtnLabel) {
			@Override
			public void onClick() {
				hide();
				ShatteredPixelDungeon.scene().addToFront(new WndTextInput(
						Messages.get(WndUsername.class, "dialog_register_title"),
						Messages.get(WndUsername.class, "dialog_register_prompt"),
						SPDSettings.customUsername(),
						16,
						false,
						Messages.get(WndUsername.class, "btn_confirm"),
						Messages.get(WndUsername.class, "btn_cancel")
				) {
					@Override
					public void onSelect(boolean positive, String text) {
						if (positive && text != null && !text.trim().isEmpty()) {
							final String cleanName = text.trim();
							if (!UsernameService.isValidUsername(cleanName)) {
								ShatteredPixelDungeon.scene().addToFront(new WndMessage(Messages.get(WndUsername.class, "error_invalid_format")));
								return;
							}

							UsernameService.registerUsernameAsync(cleanName, new UsernameService.Callback() {
								@Override
								public void onComplete(final UsernameService.Result result, final String username, final String accountKey, final String message) {
									Gdx.app.postRunnable(new Runnable() {
										@Override
										public void run() {
											if (result == UsernameService.Result.SUCCESS) {
												String successMsg = Messages.get(WndUsername.class, "success_registered", username, accountKey);
												ShatteredPixelDungeon.scene().addToFront(new WndMessage(successMsg));
											} else if (result == UsernameService.Result.ALREADY_TAKEN) {
												ShatteredPixelDungeon.scene().addToFront(new WndMessage(Messages.get(WndUsername.class, "error_taken")));
											} else {
												ShatteredPixelDungeon.scene().addToFront(new WndMessage(Messages.get(WndUsername.class, "error_network")));
											}
											if (onDismissCallback != null) onDismissCallback.run();
										}
									});
								}
							});
						} else if (!positive) {
							ShatteredPixelDungeon.scene().addToFront(new WndUsername(onDismissCallback));
						}
					}
				});
			}
		};
		btnRegister.setRect(0, pos, width, 18);
		add(btnRegister);
		pos = btnRegister.bottom() + GAP;

		// Button to link an existing username with account key on this device
		RedButton btnLink = new RedButton(Messages.get(this, "btn_link_device")) {
			@Override
			public void onClick() {
				hide();
				ShatteredPixelDungeon.scene().addToFront(new WndTextInput(
						Messages.get(WndUsername.class, "dialog_link_username_title"),
						Messages.get(WndUsername.class, "dialog_link_username_prompt"),
						"",
						16,
						false,
						Messages.get(WndUsername.class, "btn_next"),
						Messages.get(WndUsername.class, "btn_cancel")
				) {
					@Override
					public void onSelect(boolean positive, final String usernameInput) {
						if (positive && usernameInput != null && !usernameInput.trim().isEmpty()) {
							final String cleanUsername = usernameInput.trim();
							ShatteredPixelDungeon.scene().addToFront(new WndTextInput(
									Messages.get(WndUsername.class, "dialog_link_key_title"),
									Messages.get(WndUsername.class, "dialog_link_key_prompt", cleanUsername),
									"",
									32,
									false,
									Messages.get(WndUsername.class, "btn_link_confirm"),
									Messages.get(WndUsername.class, "btn_cancel")
							) {
								@Override
								public void onSelect(boolean posKey, String keyInput) {
									if (posKey && keyInput != null && !keyInput.trim().isEmpty()) {
										final String cleanKey = keyInput.trim();
										UsernameService.linkExistingUsernameAsync(cleanUsername, cleanKey, new UsernameService.Callback() {
											@Override
											public void onComplete(final UsernameService.Result result, final String username, final String accountKey, final String message) {
												Gdx.app.postRunnable(new Runnable() {
													@Override
													public void run() {
														if (result == UsernameService.Result.SUCCESS) {
															ShatteredPixelDungeon.scene().addToFront(new WndMessage(Messages.get(WndUsername.class, "success_linked", username)));
														} else if (result == UsernameService.Result.NOT_FOUND) {
															ShatteredPixelDungeon.scene().addToFront(new WndMessage(Messages.get(WndUsername.class, "error_not_found")));
														} else if (result == UsernameService.Result.INVALID_KEY) {
															ShatteredPixelDungeon.scene().addToFront(new WndMessage(Messages.get(WndUsername.class, "error_invalid_key")));
														} else {
															ShatteredPixelDungeon.scene().addToFront(new WndMessage(Messages.get(WndUsername.class, "error_network")));
														}
														if (onDismissCallback != null) onDismissCallback.run();
													}
												});
											}
										});
									} else if (!posKey) {
										ShatteredPixelDungeon.scene().addToFront(new WndUsername(onDismissCallback));
									}
								}
							});
						} else if (!positive) {
							ShatteredPixelDungeon.scene().addToFront(new WndUsername(onDismissCallback));
						}
					}
				});
			}
		};
		btnLink.setRect(0, pos, width, 18);
		add(btnLink);
		pos = btnLink.bottom() + GAP;

		resize(width, (int)pos);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (onDismissCallback != null) {
			onDismissCallback.run();
		}
	}
}
