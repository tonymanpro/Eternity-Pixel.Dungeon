/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndUseItem;

public class ItemJournalButton extends IconButton {

	private final Item item;
	private final Window parentWnd;

	public ItemJournalButton(Item item, Window parentWnd) {
		super(Icons.JOURNAL.get());
		this.item = item;
		this.parentWnd = parentWnd;
	}

	@Override
	public void onClick() {
		super.onClick();
		customNote();
	}

	private void customNote() {
		Notes.CustomRecord note;

		if (item instanceof EquipableItem) {
			note = Notes.findCustomRecord(((EquipableItem) item).customNoteID);
		} else {
			note = Notes.findCustomRecord(item.getClass());
		}

		if (note == null) {
			if (Notes.getRecords(Notes.CustomRecord.class).size() >= Notes.customRecordLimit()) {
				GameScene.show(new WndTitledMessage(Icons.INFO.get(),
						Messages.get(CustomNoteButton.class, "limit_title"),
						Messages.get(CustomNoteButton.class, "limit_text")));
				return;
			}

			note = new Notes.CustomRecord(item, "", "");
			if (item instanceof EquipableItem) {
				note.assignID();
				((EquipableItem) item).customNoteID = note.ID();
			}

			addNote(note,
					Messages.get(CustomNoteButton.class, "new_inv"),
					Messages.get(CustomNoteButton.class, "new_item_title", Messages.titleCase(item.name())));
		} else {
			GameScene.show(new CustomNoteButton.CustomNoteWindow(note));
		}
	}

	private void addNote(final Notes.CustomRecord note, String promptTitle, String promptText) {
		GameScene.show(new WndTextInput(promptTitle,
				promptText,
				"",
				50,
				false,
				Messages.get(CustomNoteButton.CustomNoteWindow.class, "confirm"),
				Messages.get(CustomNoteButton.CustomNoteWindow.class, "cancel")) {
			@Override
			public void onSelect(boolean positive, String text) {
				if (positive && !text.isEmpty()) {
					Notes.add(note);
					note.editText(text, "");

					if (parentWnd != null) {
						parentWnd.hide();
					}

					hide();
					if (parentWnd instanceof WndUseItem) {
						WndUseItem useWnd = (WndUseItem) parentWnd;
						GameScene.show(new WndUseItem(useWnd.owner, useWnd.item));
					} else {
						GameScene.show(new CustomNoteButton.CustomNoteWindow(note));
					}
				}
			}
		});
	}
}
