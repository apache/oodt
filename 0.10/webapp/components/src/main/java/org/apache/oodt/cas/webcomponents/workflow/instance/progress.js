/* WebAppers Progress Bar, version 0.2
* (c) 2007 Ray Cheung
*
* WebAppers Progress Bar is freely distributable under the terms of an Creative Commons license.
* For details, see the WebAppers web site: http://www.webappers.com/progressBar/
*
/*--------------------------------------------------------------------------*/

var initial = -120;
var imageWidth=240;
var eachPercent = (imageWidth/2)/100;
/************************************************************\
*
\************************************************************/
function setText (id, percent)
{
    $(id+'Text').innerHTML = percent+"%";
}
/************************************************************\
*
\************************************************************/
function display (img, id, percentage,color )
{	
	if (typeof color == "undefined") {
    color = "1";
  	}
    var percentageWidth = eachPercent * percentage;
    var actualWidth = initial + percentageWidth ;
    document.write('<img id="'+id+'" src="'+img+'" alt="'+percentage+'%" class="percentImage'+color+'" style="background-position: '+actualWidth+'px 0pt;"/> <span id="'+id+'Text">'+percentage+'%</span>');
}
/************************************************************\
*
\************************************************************/
function emptyProgress(id)
{
    var newProgress = initial+'px';
    $(id).style.backgroundPosition=newProgress+' 0';
    setText(id,'0');
}
/************************************************************\
*
\************************************************************/
function getProgress(id)
{
    var nowWidth = $(id).style.backgroundPosition.split("px");
    return (Math.floor(100+(nowWidth[0]/eachPercent))+'%');
	
}
/************************************************************\
*
\************************************************************/
function setProgress(id, percentage)
{
    var percentageWidth = eachPercent * percentage;
    var newProgress = eval(initial)+eval(percentageWidth)+'px';
    $(id).style.backgroundPosition=newProgress+' 0';
    setText(id,percentage);
}
/************************************************************\
*
\************************************************************/
function plus ( id, percentage )
{
    var nowWidth = $(id).style.backgroundPosition.split("px");
    var nowPercent = Math.floor(100+(nowWidth[0]/eachPercent))+eval(percentage);
    var percentageWidth = eachPercent * percentage;
    var actualWidth = eval(nowWidth[0]) + eval(percentageWidth);
    var newProgress = actualWidth+'px';
    if(actualWidth>=0 && percentage <100)
    {
        var newProgress = 1+'px';
        $(id).style.backgroundPosition=newProgress+' 0';
        setText(id,100);
        alert('full');
    }
    else
    {
        $(id).style.backgroundPosition=newProgress+' 0';
        setText(id,nowPercent);
    }
}
/************************************************************\
*
\************************************************************/
function minus ( id, percentage )
{
    var nowWidth = $(id).style.backgroundPosition.split("px");
    var nowPercent = Math.floor(100+(nowWidth[0]/eachPercent))-eval(percentage);
    var percentageWidth = eachPercent * percentage;
    var actualWidth = eval(nowWidth[0]) - eval(percentageWidth);
    var newProgress = actualWidth+'px';
    if(actualWidth<=-120)
    {
        var newProgress = -120+'px';
        $(id).style.backgroundPosition=newProgress+' 0';
        setText(id,0);
        alert('empty');
    }
    else
    {
        $(id).style.backgroundPosition=newProgress+' 0';
        setText(id,nowPercent);
    }
}
/************************************************************\
*
\************************************************************/
function fillProgress(id, endPercent)
{
    var nowWidth = $(id).style.backgroundPosition.split("px");
    startPercent = Math.ceil(100+(nowWidth[0]/eachPercent))+1;
    var actualWidth = initial + (eachPercent * endPercent);
    if (startPercent <= endPercent && nowWidth[0] <= actualWidth)
    {
        plus(id,'1');
        setText(id,startPercent);
        setTimeout("fillProgress('"+id+"',"+endPercent+")",10);
    }
}
