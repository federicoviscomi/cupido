/*  Cupido - An online Hearts game.
 *  Copyright (C) 2011 Lorenzo Belli, Marco Poletti, Federico Viscomi
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package unibo.as.cupido.client.screens;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import unibo.as.cupido.client.CometMessageListener;
import unibo.as.cupido.client.Cupido;
import unibo.as.cupido.client.CupidoInterfaceAsync;
import unibo.as.cupido.client.widgets.ChatWidget;
import unibo.as.cupido.client.widgets.ChatWidget.ChatListener;
import unibo.as.cupido.common.exception.FatalException;
import unibo.as.cupido.common.exception.MaxNumTableReachedException;
import unibo.as.cupido.common.exception.UserNotAuthenticatedException;
import unibo.as.cupido.common.structures.ChatMessage;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.common.structures.RankingEntry;
import unibo.as.cupido.common.structures.TableInfoForClient;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class MainMenuScreen extends AbsolutePanel implements Screen {

	// The interval between subsequent polls to the (global) chat, in
	// milliseconds.
	private final static int chatRefreshInterval = 2000;

	/**
	 * The width of the chat sidebar.
	 */
	public static final int chatWidth = 300;

	private List<PushButton> buttons = new ArrayList<PushButton>();

	private Timer chatTimer;
	private ChatWidget chatWidget;

	private CupidoInterfaceAsync cupidoService;

	private boolean frozen = false;

	/**
	 * This is true if the user sent a message and no refresh request has yet
	 * been sent to the servlet after that.
	 */
	private boolean needRefresh = false;

	private ScreenManager screenManager;

	private boolean stoppedRefreshing = false;

	private String username;

	private boolean waitingServletResponse = false;

	public MainMenuScreen(final ScreenManager screenManager,
			final String username, final CupidoInterfaceAsync cupidoService) {

		this.screenManager = screenManager;
		this.username = username;
		this.cupidoService = cupidoService;

		setHeight(Cupido.height + "px");
		setWidth(Cupido.width + "px");

		VerticalPanel panel = new VerticalPanel();
		panel.setSpacing(30);
		panel.setWidth((Cupido.width - chatWidth) + "px");
		panel.setHeight((Cupido.height - 100) + "px");
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		add(panel, 0, 0);

		// Set an empty listener (one that handles no messages).
		screenManager.setListener(new CometMessageListener());

		Label label = new HTML("<h1>Menu</h1>");
		panel.add(label);

		PushButton tableButton = new PushButton("Crea un nuovo tavolo");
		tableButton.setWidth("250px");
		buttons.add(tableButton);
		tableButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				handleCreateTable();
			}
		});
		panel.add(tableButton);

		PushButton tableListButton = new PushButton("Vai alla lista dei tavoli");
		tableListButton.setWidth("250px");
		buttons.add(tableListButton);
		tableListButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				handleDisplayTableList();
			}
		});
		panel.add(tableListButton);

		PushButton scoresButton = new PushButton("Vai alla schermata Punteggi");
		scoresButton.setWidth("250px");
		buttons.add(scoresButton);
		scoresButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				handleDisplayScores();
			}
		});
		panel.add(scoresButton);

		PushButton aboutButton = new PushButton("Informazioni su Cupido");
		aboutButton.setWidth("250px");
		buttons.add(aboutButton);
		aboutButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				screenManager.displayAboutScreen(username);
			}
		});
		panel.add(aboutButton);

		PushButton logoutButton = new PushButton("Logout");
		logoutButton.setWidth("250px");
		buttons.add(logoutButton);
		logoutButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				handleLogout();
			}
		});
		panel.add(logoutButton);

		chatWidget = new ChatWidget(chatWidth, Cupido.height,
				new ChatListener() {
					@Override
					public void sendMessage(String message) {
						cupidoService.sendGlobalChatMessage(message,
								new AsyncCallback<Void>() {
									@Override
									public void onFailure(Throwable caught) {
										try {
											throw caught;
										} catch (IllegalArgumentException e) {
											// FIXME: Can this happen?
											screenManager
													.displayGeneralErrorScreen(e);
										} catch (UserNotAuthenticatedException e) {
											screenManager
													.displayGeneralErrorScreen(e);
										} catch (FatalException e) {
											screenManager
													.displayGeneralErrorScreen(e);
										} catch (Throwable e) {
											assert false;
										}
									}

									@Override
									public void onSuccess(Void result) {
										needRefresh = true;
										chatTimer.cancel();
										chatTimer.run();
									}
								});
					}
				});
		add(chatWidget, Cupido.width - chatWidth, 0);

		DOM.setStyleAttribute(chatWidget.getElement(), "borderLeftStyle",
				"solid");
		DOM.setStyleAttribute(chatWidget.getElement(), "borderLeftWidth", "1px");

		chatTimer = new Timer() {
			@Override
			public void run() {
				if (frozen)
					return;
				if (waitingServletResponse)
					return;
				needRefresh = false;
				cupidoService
						.viewLastMessages(new AsyncCallback<ChatMessage[]>() {
							@Override
							public void onFailure(Throwable caught) {
								if (frozen)
									return;

								waitingServletResponse = false;
								try {
									throw caught;
								} catch (UserNotAuthenticatedException e) {
									screenManager.displayGeneralErrorScreen(e);
								} catch (FatalException e) {
									screenManager.displayGeneralErrorScreen(e);
								} catch (Throwable e) {
									// Should never get here.
									assert false;
								}
							}

							@Override
							public void onSuccess(ChatMessage[] messages) {
								if (frozen)
									return;

								waitingServletResponse = false;

								chatWidget.setLastMessages(messages);

								if (!stoppedRefreshing) {
									if (needRefresh)
										// Refresh immediately.
										chatTimer.run();
									else
										chatTimer.schedule(chatRefreshInterval);
								}
							}
						});
				waitingServletResponse = true;
			}
		};
		chatTimer.run();
	}

	@Override
	public void freeze() {
		for (PushButton w : buttons)
			w.setEnabled(false);
		chatWidget.freeze();
		frozen = true;
	}

	private void handleCreateTable() {
		freeze();
		cupidoService.createTable(new AsyncCallback<InitialTableStatus>() {
			@Override
			public void onFailure(Throwable caught) {
				try {
					throw caught;
					/*
					 * TODO: Enable this when this exception is thrown. } catch
					 * (MaxNumTableReachedException e) {
					 * screenManager.displayMainMenuScreen(username);
					 * Window.alert(
					 * "\310 stato raggiunto il numero massimo di tavoli supportati. Riprova pi\371 tardi."
					 * );
					 */
				} catch (Throwable e) {
					screenManager.displayGeneralErrorScreen(caught);
				}
			}

			@Override
			public void onSuccess(final InitialTableStatus initialTableStatus) {
				// Get the user's score, too.
				// The screen is already frozen, so there's no
				// need to freeze it again.
				cupidoService.getMyRank(new AsyncCallback<RankingEntry>() {
					@Override
					public void onFailure(Throwable caught) {
						screenManager.displayGeneralErrorScreen(caught);
					}

					@Override
					public void onSuccess(RankingEntry rankingEntry) {
						screenManager.displayTableScreen(username, true,
								initialTableStatus, rankingEntry.points);
					}
				});
			}
		});
	}

	private void handleDisplayScores() {
		freeze();
		cupidoService.getTopRank(new AsyncCallback<ArrayList<RankingEntry>>() {
			@Override
			public void onFailure(Throwable caught) {
				screenManager.displayGeneralErrorScreen(caught);
			}

			@Override
			public void onSuccess(final ArrayList<RankingEntry> topRanks) {
				// The screen is already frozen, there's no need
				// to freeze it again.
				cupidoService
						.getLocalRank(new AsyncCallback<ArrayList<RankingEntry>>() {
							@Override
							public void onFailure(Throwable caught) {
								screenManager.displayGeneralErrorScreen(caught);
							}

							@Override
							public void onSuccess(
									ArrayList<RankingEntry> localRanks) {
								screenManager.displayScoresScreen(username,
										topRanks, localRanks);
							}
						});
			}
		});
	}

	private void handleDisplayTableList() {
		freeze();
		cupidoService
				.getTableList(new AsyncCallback<Collection<TableInfoForClient>>() {
					@Override
					public void onFailure(Throwable caught) {
						screenManager.displayGeneralErrorScreen(caught);
					}

					@Override
					public void onSuccess(Collection<TableInfoForClient> result) {
						screenManager.displayTableListScreen(username, result);
					}
				});
	}

	private void handleLogout() {
		freeze();
		cupidoService.logout(new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable caught) {
				screenManager.displayGeneralErrorScreen(caught);
			}

			@Override
			public void onSuccess(Void result) {
				screenManager.displayLoginScreen();
			}
		});
	}

	@Override
	public void prepareRemoval() {
		stoppedRefreshing = true;
		chatTimer.cancel();
	}
}
