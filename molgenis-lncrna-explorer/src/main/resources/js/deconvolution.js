var SelectClass = React.createClass({
	displayName : 'Select',
	propTypes : {
		options : React.PropTypes.array.isRequired,
		onChange : React.PropTypes.func,
		value : React.PropTypes.string
	},
	render : function() {
		return React.DOM.select({
			multiple : false,
			value : this.props.value,
			onChange : this.props.onChange,
			className : 'form-control'
		}, this.props.options.map(function(option) {
			return React.DOM.option({
				"value" : option,
				"label" : option,
				"key" : option
			});
		}));
	}
});

var Select = React.createFactory(SelectClass);

var SelectFormGroupClass = React.createClass({
	displayName : 'SelectFormGroup',
	propTypes : {
		options : React.PropTypes.array.isRequired,
		onChange : React.PropTypes.func,
		value : React.PropTypes.string,
		label : React.PropTypes.string.isRequired
	},
	render : function() {
		return React.DOM.div({
			'className' : 'form-group'
		}, React.DOM.label(null, this.props.label), Select({
			value : this.props.value,
			onChange : this.props.onChange,
			options : this.props.options
		}));
	}
});

var SelectFormGroup = React.createFactory(SelectFormGroupClass);

var DeconvolutionFiltersClass = React.createClass({
	displayName : 'DeconvolutionFilters',
	propTypes : {
		filters : React.PropTypes.array.isRequired,
	},
	render : function() {
		var form = React.DOM.form({
			'className' : 'form-inline'
		}, this.props.filters.map(function(filter) {
			return SelectFormGroup({
				value : filter.value,
				options : filter.options,
				onChange : filter.onChange,
				label : filter.label,
				key : filter.label
			});
		}));
		var header = React.DOM.div({
			className : 'panel-heading'
		}, React.DOM.h3({
			className : 'panel-title'
		}, "filters"));
		var body = React.DOM.div({
			className : 'panel-body'
		}, form);
		return React.DOM.div({
			className : 'panel panel-primary'
		}, header, body);
	}
});

var DeconvolutionFilters = React.createFactory(DeconvolutionFiltersClass);

var DeconvolutionPlotClass = React.createClass({
	displayName : 'DeconvolutionPlot',
	propTypes : {
		image : React.PropTypes.string.isRequired,
		caption : React.PropTypes.string
	},
	render : function() {
		return React.DOM.div({
			className : 'thumbnail'
		}, React.DOM.img({
			className : 'img-responsive',
			src : this.props.image
		}), React.DOM.div({
			className : 'caption'
		}, React.DOM.h3({}, this.props.caption)));
	}
});

var DeconvolutionPlot = React.createFactory(DeconvolutionPlotClass);

var DeconvolutionPlotsClass = React.createClass({
	displayName : 'DeconvolutionPlots',
	propTypes : {
		plots : React.PropTypes.array.isRequired,
	},
	render : function() {
		var plotComponents = this.props.plots.map(function(plot) {
			console.log(plot);
			return DeconvolutionPlot({
				"image" : plot.image.url,
				"key" : plot.name,
				'caption' : plot.name
			});
		});
		return React.DOM.div(null, plotComponents);
	}
});

var DeconvolutionPlots = React.createFactory(DeconvolutionPlotsClass);

var div = React.DOM.div;

var DeconvolutionClass = React.createClass({
	displayName : 'Deconvolution',
	getInitialState : function() {
		return {
			gene : "",
			snp : "",
			disease : "",
			geneOptions : [""],
			snpOptions : [""],
			diseaseOptions : [""],
			plots : []
		};
	},
	componentDidMount : function() {
		this._updateOptions();
	},
	_updateOptions : function() {
		var that = this;
		getGenes(this.state, function(options) {
			options.unshift('');
			that.setState({
				'geneOptions' : options
			});
		});
		getSnps(this.state, function(options) {
			options.unshift('');
			that.setState({
				'snpOptions' : options
			});
		});
		getDiseases(this.state, function(options) {
			options.unshift('');
			that.setState({
				'diseaseOptions' : options
			});
		});
		getPlots(this.state, function(plots) {
			that.setState({
				'plots' : plots
			});
		});
	},
	render : function() {
		var that = this;
		return div({
			className : 'container'
		}, div({
			className : 'row',
			key : 'filters'
		}, div({
			className : 'col-md-12'
		}, DeconvolutionFilters({
			filters : [{
				name : 'disease',
				label : 'Disease:',
				onChange : function(event) {
					that.setState({
						'disease' : event.target.value
					}, that._updateOptions);
				},
				options : that.state.diseaseOptions,
				value : that.state.disease
			}, {
				name : 'gene',
				label : 'Gene:',
				onChange : function(event) {
					that.setState({
						'gene' : event.target.value
					}, that._updateOptions);
				},
				options : that.state.geneOptions,
				value : that.state.gene
			}, {
				name : 'snp',
				label : 'SNP:',
				onChange : function(event) {
					that.setState({
						'snp' : event.target.value
					}, that._updateOptions);
				},
				options : that.state.snpOptions,
				value : that.state.snp
			}]
		}))), DeconvolutionPlots({
			plots : this.state.plots
		}))
	}
});

var Deconvolution = React.createFactory(DeconvolutionClass);

function toRsqlValue(value) {
	var rsqlValue;
	if (value.indexOf('"') !== -1 || value.indexOf('\'') !== -1
			|| value.indexOf('(') !== -1 || value.indexOf(')') !== -1
			|| value.indexOf(';') !== -1 || value.indexOf(',') !== -1
			|| value.indexOf('=') !== -1 || value.indexOf('!') !== -1
			|| value.indexOf('~') !== -1 || value.indexOf('<') !== -1
			|| value.indexOf('>') !== -1 || value.indexOf(' ') !== -1) {
		rsqlValue = '"' + encodeURIComponent(value) + '"';
	} else {
		rsqlValue = encodeURIComponent(value);
	}
	return rsqlValue;
};

function getUri(attrs, num, filters) {
	var uri = 'http://localhost:8080/api/v2/DeconvolutionPlot?attrs=' + attrs
			+ '&num=' + num;
	var query = [];
	if (filters.disease) {
		query.push('disease==' + toRsqlValue(filters.disease));
	}
	if (filters.gene) {
		query.push('gene==' + toRsqlValue(filters.gene));
	}
	if (filters.snp) {
		query.push('snp==' + toRsqlValue(filters.snp));
	}
	queryString = query.join(';');
	if(queryString.length){
		uri = uri + '&q=' + queryString;
	}
	return uri;
}

function getSnps(state, callback) {
	var uri = getUri('snp', 123, {
		disease : state.disease,
		gene : state.gene
	});
	$.get(uri).done(function(data) {
		callback($.unique(data.items.map(function(plot) {
			return plot.snp;
		})));
	});
}

function getGenes(state, callback) {
	var uri = getUri('gene', 123, {
		disease : state.disease,
		snp : state.snp
	});
	$.get(uri).done(function(data) {
		callback($.unique(data.items.map(function(plot) {
			return plot.gene;
		})));
	});
}

function getDiseases(state, callback) {
	var uri = getUri('disease', 123, {
		gene : state.gene,
		snp : state.snp
	});
	$.get(uri).done(function(data) {
		callback($.unique(data.items.map(function(plot) {
			return plot.disease;
		})));
	});
}

function getPlots(state, callback) {
	var uri = getUri('name,snp,gene,disease,image', 123, state);
	$.get(uri).done(function(data) {
		callback(data.items);
	});
}

$(function() {
	React.render(Deconvolution({}), $('#deconvolution')[0]);
});