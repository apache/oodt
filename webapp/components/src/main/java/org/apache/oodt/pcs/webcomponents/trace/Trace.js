var openImg = new Image();
openImg.src = "./images/open.gif";
var closedImg = new Image();
closedImg.src = "./images/closed.gif";

function showBranch(branch) {
	var objBranch = document.getElementById(branch).style;
	if (objBranch.display == "block")
		objBranch.display = "none";
	else
		objBranch.display = "block";
}

function swapFolder(img) {
	objImg = document.getElementById(img);
	if (objImg.src.indexOf('closed') > -1)
		objImg.src = openImg.src;
	else
		objImg.src = closedImg.src;
}