package de.terrestris.geoserver.graphql.web;

import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.javascript.DefaultJavaScriptCompressor;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.model.IModel;
import org.apache.wicket.resource.NoOpTextCompressor;
import org.geoserver.catalog.PublishedInfo;
import org.geoserver.web.GeoServerApplication;
import org.geoserver.web.publish.PublishedConfigurationPanel;
import org.geotools.util.logging.Logging;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

public class GraphqlPanel extends PublishedConfigurationPanel<PublishedInfo> implements IHeaderContributor {

    private static final Logger LOGGER = Logging.getLogger(GraphqlPanel.class);

    public GraphqlPanel(String id, IModel<PublishedInfo> model) {
        super(id, model);
        setMarkupId("graphql");
        LOGGER.info("Created a new instance of the graphql tab.");
    }

    /**
     * Add required CSS and Javascript resources
     */
    @Override
    public void renderHead(IHeaderResponse header) {
        super.renderHead(header);
        try {
            renderHeaderCss(header);
            renderHeaderScript(header);
        } catch (IOException | TemplateException e) {
            throw new WicketRuntimeException(e);
        }
    }

    /**
     * Renders header CSS
     */
    private void renderHeaderCss(IHeaderResponse header) throws IOException, TemplateException {
        header.render(CssHeaderItem.forUrl("https://unpkg.com/graphiql/graphiql.min.css"));
        header.render(CssHeaderItem.forCSS("#graphql {height: 700px;}", "graphql-css"));
    }

    /**
     * Renders header scripts
     */
    private void renderHeaderScript(IHeaderResponse header) throws IOException, TemplateException {
        Map<String, Object> context = new HashMap<>();

        InputStream in = GraphqlPanel.class.getResourceAsStream("graphql.ftl");
        String script = IOUtils.toString(Objects.requireNonNull(in), StandardCharsets.UTF_8);

        // temporarily disable javascript compression since build resources are already compressed
        GeoServerApplication.get()
            .getResourceSettings()
            .setJavaScriptCompressor(new NoOpTextCompressor());
        header.render(JavaScriptHeaderItem.forUrl("https://unpkg.com/react/umd/react.production.min.js"));
        header.render(JavaScriptHeaderItem.forUrl("https://unpkg.com/react-dom/umd/react-dom.production.min.js"));
        header.render(JavaScriptHeaderItem.forUrl("https://unpkg.com/graphiql/graphiql.min.js"));
        header.render(OnLoadHeaderItem.forScript(script));
    }

    /**
     * As soon as the {@link GraphqlPanel} is removed the default Javascript compression needs to be
     * enabled
     */
    @Override
    protected void onRemove() {
        // (re-) enable javascript compression
        GeoServerApplication.get()
            .getResourceSettings()
            .setJavaScriptCompressor(new DefaultJavaScriptCompressor());
        super.onRemove();
    }

}
