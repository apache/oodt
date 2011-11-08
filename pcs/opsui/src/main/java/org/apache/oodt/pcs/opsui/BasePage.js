function menuSelected(cell) {
	cell.bgColor = "#006699";
}
function menuNotSelected(cell) {
	cell.bgColor = "#666666";
}
function subMenuSelected(id) {
	var x = $(id).rows;
	var y = x[0].cells;
	y[0].innerHTML = "<img src='/images/selleftcorner.gif'>";
	y[1].bgColor = "#99CCFF";
	y[2].innerHTML = "<img src='/images/selrightcorner.gif'>";
}

function subMenuNotSelected(id) {
	var x = $(id).rows;
	var y = x[0].cells;
	y[0].innerHTML = "<img src='/images/leftcorner.gif'>";
	y[1].bgColor = "#cccccc";
	y[2].innerHTML = "<img src='/images/rightcorner.gif'>";
}

var interfaceimg = new Array()
function preloadimg() {
	for (i = 0; i < arguments.length; i++) {
		interfaceimg[i] = new Image()
		interfaceimg[i].src = arguments[i]
	}
}

// Enter full URL to image tabs to preload below. Separate with comma:

preloadimg("/images/rightcorner.gif", "/images/leftcorner.gif",
		"/images/selrightcorner.gif", "/images/selleftcorner.gif")