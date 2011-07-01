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
import unibo.as.cupido.common.exception.FullTableException;
import unibo.as.cupido.common.exception.GameInterruptedException;
import unibo.as.cupido.common.exception.NoSuchTableException;
import unibo.as.cupido.common.exception.WrongGameStateException;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.common.structures.ObservedGameStatus;
import unibo.as.cupido.common.structures.RankingEntry;
import unibo.as.cupido.common.structures.TableDescriptor;
import unibo.as.cupido.common.structures.TableInfoForClient;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * This class manages the table list screen, displaying a list of tables
 * that the user can join or view.
 */
public class TableListScreen extends VerticalPanel implements Screen {

	/**
	 * This class is used for rendering a single row in the list.
	 */
	private class TableCell extends AbstractCell<TableInfoForClient> {
		@Override
		public void render(com.google.gwt.cell.client.Cell.Context context,
				TableInfoForClient value, SafeHtmlBuilder sb) {
			if (value != null) {
				sb.appendHtmlConstant("Creatore: <b>");
				sb.appendEscaped(value.creator);
				sb.appendHtmlConstant("</b></br>");
				sb.appendHtmlConstant("Numero posti liberi: ");
				sb.append(value.freePosition);
				sb.appendHtmlConstant("</br>");
			}
		}
	}
	
	/**
	 * The widget that displays the list of available tables.
	 */
	private CellList<TableInfoForClient> cellList;
	
	/**
	 * Specifies whether the UI is frozen (i.e. does no longer react to events) or not.
	 */
	private boolean frozen = false;

	/**
	 * The button that allows the user to join the selected table.
	 */
	private PushButton joinButton;

	/**
	 * The button that allows the user to go back to the main menu.
	 */
	private PushButton menuButton;
	
	/**
	 * A list containing data about the available tables.
	 */
	private List<TableInfoForClient> tableList;

	/**
	 * The button that allows the user to view the selected table.
	 */
	private PushButton viewButton;

	/**
	 * @param screenManager The global screen manager.
	 * @param username The username of the current user.
	 * @param tableCollection The list of tables available for joining and/or viewing.
	 * @param cupidoService This is used to communicate with the servlet using RPC.
	 */
	public TableListScreen(final ScreenManager screenManager,
			final String username,
			Collection<TableInfoForClient> tableCollection,
			final CupidoInterfaceAsync cupidoService) {

		// Set an empty listener (one that handles no messages).
		screenManager.setListener(new CometMessageListener());

		setHeight((Cupido.height - 80) + "px");
		setWidth((Cupido.width - 120) + "px");

		setHorizontalAlignment(ALIGN_CENTER);

		DOM.setStyleAttribute(getElement(), "marginLeft", "60px");

		add(new HTML("<h1>Lista tavoli</h1>"));

		tableList = new ArrayList<TableInfoForClient>();

		for (TableInfoForClient x : tableCollection)
			tableList.add(x);

		cellList = new CellList<TableInfoForClient>(new TableCell());
		cellList.setRowCount(tableList.size());
		cellList.setRowData(0, tableList);

		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("350px");
		panel.setHeight("400px");
		panel.setHorizontalAlignment(ALIGN_CENTER);
		panel.setVerticalAlignment(ALIGN_MIDDLE);
		panel.setSpacing(30);
		panel.add(new HTML(
				"<p>Non &egrave; presente nessun tavolo.</p>"
						+ "<p>Riprova pi&ugrave; tardi, o torna al menu principale e crea un nuovo tavolo.</p>"));

		cellList.setEmptyListWidget(panel);

		final SingleSelectionModel<TableInfoForClient> selectionModel = new SingleSelectionModel<TableInfoForClient>();
		selectionModel
				.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
					@Override
					public void onSelectionChange(SelectionChangeEvent event) {
						if (frozen)
							return;

						TableInfoForClient table = selectionModel
								.getSelectedObject();
						if (table == null)
							return;
						boolean canJoin = (table.freePosition != 0);
						joinButton.setEnabled(canJoin);
					}
				});
		cellList.setSelectionModel(selectionModel);
		cellList.setWidth("350px");
		cellList.setHeight("400px");
		add(cellList);

		DOM.setStyleAttribute(cellList.getElement(), "borderStyle", "solid");
		DOM.setStyleAttribute(cellList.getElement(), "borderWidth", "1px");

		HorizontalPanel bottomPanel = new HorizontalPanel();
		bottomPanel.setHorizontalAlignment(ALIGN_CENTER);
		bottomPanel.setSpacing(50);
		add(bottomPanel);

		menuButton = new PushButton("Torna al menu");
		menuButton.setWidth("100px");
		menuButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				screenManager.displayMainMenuScreen(username);
			}
		});
		bottomPanel.add(menuButton);

		viewButton = new PushButton("Guarda");
		viewButton.setWidth("100px");
		viewButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				TableInfoForClient tableInfoForClient = selectionModel
						.getSelectedObject();
				freeze();
				TableDescriptor descriptor = tableInfoForClient.tableDescriptor;
				cupidoService.viewTable(descriptor.ltmId, descriptor.id,
						new AsyncCallback<ObservedGameStatus>() {
							@Override
							public void onFailure(Throwable caught) {
								try {
									throw caught;
								} catch (NoSuchTableException e) {
									screenManager
											.displayMainMenuScreen(username);
									Window.alert("Il tavolo che volevi guardare non esiste pi\371.");
								} catch (WrongGameStateException e) {
									screenManager
											.displayMainMenuScreen(username);
									Window.alert("Il tavolo che volevi guardare non esiste pi\371.");
								} catch (GameInterruptedException e) {
									screenManager
											.displayMainMenuScreen(username);
									Window.alert("Il tavolo che volevi guardare non esiste pi\371.");
								} catch (Throwable e) {
									screenManager.displayGeneralErrorScreen(e);
								}
							}

							@Override
							public void onSuccess(
									ObservedGameStatus observedGameStatus) {
								// TODO: Can Comet notifications arrive before
								// the screen is switched?
								screenManager.displayObservedTableScreen(
										username, observedGameStatus);
							}
						});
			}
		});
		bottomPanel.add(viewButton);

		joinButton = new PushButton("Gioca");
		joinButton.setWidth("100px");
		joinButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final TableInfoForClient tableInfoForClient = selectionModel
						.getSelectedObject();
				freeze();
				// Get the user's points *before* calling join, to avoid
				// losing comet notifications after the join.
				cupidoService.getMyRank(new AsyncCallback<RankingEntry>() {
					@Override
					public void onFailure(Throwable caught) {
						screenManager.displayGeneralErrorScreen(caught);
					}

					@Override
					public void onSuccess(final RankingEntry rankingEntry) {
						TableDescriptor descriptor = tableInfoForClient.tableDescriptor;
						cupidoService.joinTable(descriptor.ltmId,
								descriptor.id,
								new AsyncCallback<InitialTableStatus>() {
									@Override
									public void onFailure(Throwable caught) {
										try {
											throw caught;
										} catch (FullTableException e) {
											screenManager
													.displayMainMenuScreen(username);
											Window.alert("Il tavolo in cui volevi entrare non ha pi\371 posti liberi.");
										} catch (NoSuchTableException e) {
											screenManager
													.displayMainMenuScreen(username);
											Window.alert("Il tavolo in cui volevi entrare non esiste pi\371.");
										} catch (GameInterruptedException e) {
											screenManager
													.displayMainMenuScreen(username);
											Window.alert("Il tavolo in cui volevi entrare non esiste pi\371.");
										} catch (Throwable e) {
											screenManager
													.displayGeneralErrorScreen(e);
										}
									}

									@Override
									public void onSuccess(
											InitialTableStatus initialTableStatus) {
										// TODO: Can Comet notifications arrive
										// before
										// that the screen is switched?
										screenManager.displayTableScreen(
												username, false,
												initialTableStatus,
												rankingEntry.points);
									}
								});
					}
				});
			}
		});
		bottomPanel.add(joinButton);

		if (cellList.getRowCount() == 0) {
			viewButton.setEnabled(false);
			joinButton.setEnabled(false);
		} else {
			cellList.getSelectionModel().setSelected(tableList.get(0), true);
		}
	}

	@Override
	public void freeze() {
		menuButton.setEnabled(false);
		viewButton.setEnabled(false);
		joinButton.setEnabled(false);
		frozen = true;
	}

	@Override
	public void prepareRemoval() {
	}
}
