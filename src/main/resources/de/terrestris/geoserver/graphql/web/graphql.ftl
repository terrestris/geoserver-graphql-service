(function() {
      const fetcher = GraphiQL.createFetcher({ url: `${window.origin}/geoserver/graphql` });

      ReactDOM.render(
        React.createElement(GraphiQL, { fetcher: fetcher }),
        document.getElementById('graphql'),
      );
})();
