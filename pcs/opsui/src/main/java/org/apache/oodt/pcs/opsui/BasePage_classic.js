/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
function menuSelected(cell) {
	cell.css({
		'background-color' : '#006699'
	});
}
function menuNotSelected(cell) {
	cell.css({
		'background-color' : '#666666'
	});
}

function subMenuSelected(id) {
	var x = document.getElementById(id).rows;
	var y = x[0].cells;
	y[0].innerHTML = "<img src='/images/selleftcorner.gif'>";
	y[1].bgColor = "#99CCFF";
	y[2].innerHTML = "<img src='/images/selrightcorner.gif'>";
}

function subMenuNotSelected(id) {
	var x = document.getElementById(id).rows;
	var y = x[0].cells;
	y[0].innerHTML = "<img src='/images/leftcorner.gif'>";
	y[1].bgColor = "#cccccc";
	y[2].innerHTML = "<img src='/images/rightcorner.gif'>";
}

var interfaceimg = new Array();
function preloadimg() {
	for ( var i = 0; i < arguments.length; i++) {
		interfaceimg[i] = new Image();
		interfaceimg[i].src = arguments[i];
	}
}

//preloadimg("/images/rightcorner.gif", "/images/leftcorner.gif", "/images/selrightcorner.gif",
//		"/images/selleftcorner.gif");
