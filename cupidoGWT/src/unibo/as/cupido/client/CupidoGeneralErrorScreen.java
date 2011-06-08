package unibo.as.cupido.client;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;

public class CupidoGeneralErrorScreen extends AbsolutePanel {

	public CupidoGeneralErrorScreen(ScreenSwitcher screenSwitcher, Exception e) {
		setHeight(Cupido.height + "px");
		setWidth(Cupido.width + "px");
		
		ScrollPanel panel = new ScrollPanel();
		panel.setHeight(Cupido.height + "px");
		panel.setWidth(Cupido.width + "px");

		SafeHtmlBuilder htmlBuilder = new SafeHtmlBuilder();
		htmlBuilder.appendHtmlConstant("<h1>Si è verificato un errore</h1>");
		htmlBuilder.appendHtmlConstant("<p>Ricaricare la pagina per tornare a Cupido.</p>");
		htmlBuilder.appendHtmlConstant("<br>Questi sono i dati relativi all'errore: l'eccezione<br>");
		htmlBuilder.appendEscaped(e.toString());
		htmlBuilder.appendHtmlConstant("<br>Si è verificata:");
		StackTraceElement[] stackTrace = e.getStackTrace();
		for (StackTraceElement stackTraceElement : stackTrace) {
			htmlBuilder.appendHtmlConstant("<br />in ");
			htmlBuilder.appendEscaped(stackTraceElement.toString());
		}
		panel.add(new HTML(htmlBuilder.toSafeHtml()));

		add(panel, 0, 0);
	}

}
