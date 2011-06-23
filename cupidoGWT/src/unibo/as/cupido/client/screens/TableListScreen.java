package unibo.as.cupido.client.screens;

import java.util.ArrayList;
import java.util.Collection;

import unibo.as.cupido.client.Cupido;
import unibo.as.cupido.client.CupidoInterfaceAsync;
import unibo.as.cupido.common.exception.FullTableException;
import unibo.as.cupido.common.exception.NoSuchTableException;
import unibo.as.cupido.common.structures.InitialTableStatus;
import unibo.as.cupido.common.structures.ObservedGameStatus;
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

public class TableListScreen extends VerticalPanel implements Screen {

	private PushButton menuButton;
	private PushButton viewButton;
	private PushButton joinButton;
	
	private boolean frozen = false;
	private ArrayList<TableInfoForClient> tableList;
	private CellList<TableInfoForClient> cellList;

	private class TableCell extends AbstractCell<TableInfoForClient> {
		@Override
		public void render(com.google.gwt.cell.client.Cell.Context context,
				TableInfoForClient value, SafeHtmlBuilder sb) {
	      if (value != null) {
	    	  sb.appendHtmlConstant("Creatore: <b>");
	          sb.appendEscaped(value.owner);
	    	  sb.appendHtmlConstant("</b></br>");
	    	  sb.appendHtmlConstant("Numero posti liberi: ");
	    	  sb.append(value.freePosition);
	    	  sb.appendHtmlConstant("</br>");
	      }
		}
	}
	
	public TableListScreen(final ScreenManager screenManager, final String username,
			Collection<TableInfoForClient> tableCollection, final CupidoInterfaceAsync cupidoService) {
		
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
		panel.add(new HTML("<p>Non &egrave; presente nessun tavolo.</p>"
				+ "<p>Riprova pi&ugrave; tardi, o torna al menu principale e crea un nuovo tavolo.</p>"));
		
		cellList.setEmptyListWidget(panel);

		final SingleSelectionModel<TableInfoForClient> selectionModel = new SingleSelectionModel<TableInfoForClient>();
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				if (frozen)
					return;
				
				TableInfoForClient table = selectionModel.getSelectedObject();
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
		
		DOM.setStyleAttribute(cellList.getElement(), "borderLeftStyle", "solid");
		DOM.setStyleAttribute(cellList.getElement(), "borderRightStyle", "solid");
		DOM.setStyleAttribute(cellList.getElement(), "borderTopStyle", "solid");
		DOM.setStyleAttribute(cellList.getElement(), "borderBottomStyle", "solid");
		
		DOM.setStyleAttribute(cellList.getElement(), "borderLeftWidth", "1px");
		DOM.setStyleAttribute(cellList.getElement(), "borderRightWidth", "1px");
		DOM.setStyleAttribute(cellList.getElement(), "borderTopWidth", "1px");
		DOM.setStyleAttribute(cellList.getElement(), "borderBottomWidth", "1px");
		
		HorizontalPanel bottomPanel = new HorizontalPanel();
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
				TableInfoForClient tableInfoForClient = selectionModel.getSelectedObject();
				freeze();
				TableDescriptor descriptor = tableInfoForClient.tableDescriptor;
				cupidoService.viewTable(descriptor.ltmId, descriptor.id, new AsyncCallback<ObservedGameStatus>() {
					@Override
					public void onFailure(Throwable caught) {
						try {
							throw caught;
						} catch (NoSuchTableException e) {
							screenManager.displayMainMenuScreen(username);
							Window.alert("Il tavolo che volevi guardare non esiste pi\371.");
						} catch (Throwable e) {
							screenManager.displayGeneralErrorScreen(e);
						}
					}

					@Override
					public void onSuccess(ObservedGameStatus observedGameStatus) {
						// TODO: Can Comet notifications arrive before the screen is switched?
						screenManager.displayObservedTableScreen(username, observedGameStatus);
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
				TableInfoForClient tableInfoForClient = selectionModel.getSelectedObject();
				freeze();
				TableDescriptor descriptor = tableInfoForClient.tableDescriptor;
				cupidoService.joinTable(descriptor.ltmId, descriptor.id, new AsyncCallback<InitialTableStatus>() {
					@Override
					public void onFailure(Throwable caught) {
						try {
							throw caught;
						} catch (FullTableException e) {
							screenManager.displayMainMenuScreen(username);
							Window.alert("Il tavolo in cui volevi entrare non ha pi\371 posti liberi.");
						} catch (NoSuchTableException e) {
							screenManager.displayMainMenuScreen(username);
							Window.alert("Il tavolo in cui volevi entrare non esiste pi\371.");
						} catch (Throwable e) {
							screenManager.displayGeneralErrorScreen(e);
						}
					}

					@Override
					public void onSuccess(InitialTableStatus initialTableStatus) {
						// TODO: Can Comet notifications arrive before the screen is switched?
						screenManager.displayTableScreen(username, false, initialTableStatus);
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
	public void prepareRemoval() {
	}

	@Override
	public void freeze() {
		menuButton.setEnabled(false);
		viewButton.setEnabled(false);
		joinButton.setEnabled(false);
		frozen = true;
	}
}
