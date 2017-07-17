#	Licensed to the Apache Software Foundation (ASF) under one
#	or more contributor license agreements.  See the NOTICE file
#	distributed with this work for additional information
#	regarding copyright ownership.  The ASF licenses this file
#	to you under the Apache License, Version 2.0 (the
#	"License"); you may not use this file except in compliance
#	with the License.  You may obtain a copy of the License at
#
#	http://www.apache.org/licenses/LICENSE-2.0
#
#	Unless required by applicable law or agreed to in writing,
#	software distributed under the License is distributed on an
#	"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#	KIND, either express or implied.  See the License for the
#	specific language governing permissions and limitations
#	under the License.


import xmlrpclib
from metadata import MetaData


class OODTWorkFlowWrapper(object):
	def __init__(self, host):
		self._server = xmlrpclib.Server(host)

	#String	    executeDynamicWorkflow(Vector<String> taskIds, Hashtable metadata)
	def executeDynamicWorkflow(self, taskIds, metadata):
		return self._server.workflowmgr.executeDynamicWorkflow(taskIds, metadata)
	#Hashtable	getConditionById(String conditionId)
	def getConditionById(self, conditionId):
		return self._server.workflowmgr.getConditionById(conditionId)
	#Hashtable	getFirstPage()
	def getFirstPage(self):
		return self._server.workflowmgr.getFirstPage()
	#Hashtable	getLastPage()
	def getLastPage(self):
		return self._server.workflowmgr.getLastPage()
	#Hashtable	getNextPage(Hashtable currentPage)
	def getNextPage(self, currentPage):
		return self._server.workflowmgr.getNextPage()
	#int	    getNumWorkflowInstances()
	def getNumWorkflowInstances(self):
		return self._server.workflowmgr.getNumWorkflowInstances()
	#int	    getNumWorkflowInstancesByStatus(String status)
	def getNumWorkflowInstancesByStatus(self, status):
		return self._server.workflowmgr.getNumWorkflowInstancesByStatus(status)
	#Hashtable	getPrevPage(Hashtable currentPage)
	def getPrevPage(self, currentPage):
		return self._server.workflowmgr.getPrevPage(currentPage)
	#Vector 	getRegisteredEvents()
	def getEventNames(self):
		return self._server.workflowmgr.getRegisteredEvents()
	#Hashtable	getTaskById(String taskId)
	def getTaskById(self, taskId):
		return self._server.workflowmgr.getTaskById(taskId)
	#Hashtable	getWorkflowById(String workflowId)
	def getWorkflowById(self, workflowId):
		return self._server.workflowmgr.getWorkflowById(workflowId)
	#double	    getWorkflowCurrentTaskWallClockMinutes(String workflowInstId)
	def getWorkflowCurrentTaskWallClockMinutes(self, workflowInstId):
		return self._server.workflowmgr.getWorkflowCurrentTaskWallClockMinutes(workflowInstId)
	#Hashtable	getWorkflowInstanceById(String wInstId)
	def getWorkflowInstanceById(self,wInstId):
		return self._server.workflowmgr.getWorkflowInstanceById(wInstId)
	#Hashtable	getWorkflowInstanceMetadata(String wInstId)
	def getWorkflowInstanceMetadata(self, wInstId):
		return self._server.workflowmgr.getWorkflowInstanceMetadata(wInstId)
	#Vector		getWorkflowInstances()
	def getWorkflowInstances(self):
		return self._server.workflowmgr.getWorkflowInstances()
	#Vector		getWorkflowInstancesByStatus(String status)
	def getWorkflowInstancesByStatus(self, status):
		return self._server.workflowmgr.getWorkflowInstancesByStatus(status)
	#Vector		getWorkflows()
	def getWorkflows(self):
		return self._server.workflowmgr.getWorkflows()
	#Vector		getWorkflowsByEvent(String eventName)
	def getWorkflowsByEvent(self, eventName):
		return self._server.workflowmgr.getWorkflowsByEvent(eventName)
	#double		getWorkflowWallClockMinutes(String workflowInstId)
	def getWorkflowWallClockMinutes(self,workflowInstId):
		return getWorkflowWallClockMinutes(workflowInstId)
	#boolean	handleEvent(String eventName, Hashtable metadata)
	def startEvent(self, eventName, metaData):
		return self._server.workflowmgr.handleEvent(eventName, metaData)
	#Hashtable	paginateWorkflowInstances(int pageNum)
	def paginateWorkflowInstances(self, pageNum):
		return self._server.workflowmgr.paginateWorkflowInstances(pageNum)
	#Hashtable	paginateWorkflowInstances(int pageNum, String status)(self, workflowInstId)
	def paginateWorkflowInstances(self, pageNum, status):
		return self._server.workflowmgr.paginateWorkflowInstances(pageNum, status)
	#boolean	pauseWorkflowInstance(String workflowInstId)
	def pauseWorkflowInstance(self, workflowInstId):
		return self._server.workflowmgr.pauseWorkflowInstance(workflowInstId)
	#boolean	resumeWorkflowInstance(String workflowInstId)
	def resumeWorkflowInstance(self, workflowInstId):
		return self._server.workflowmgr.resumeWorkflowInstance(workflowInstId)
	#boolean	setWorkflowInstanceCurrentTaskEndDateTime(String wInstId, String endDateTimeIsoStr)
	def setWorkflowInstanceCurrentTaskEndDateTime(self, workflowInstId, endDateTimeIsoStr):
		return self._server.workflowmgr.setWorkflowInstanceCurrentTaskEndDateTime(workflowInstId, endDateTimeIsoStr)
	#boolean	setWorkflowInstanceCurrentTaskStartDateTime(String wInstId, String startDateTimeIsoStr)
	def setWorkflowInstanceCurrentTaskStartDateTime(self, workflowInstId, startDateTimeIsoStr):
		return self._server.workflowmgr.setWorkflowInstanceCurrentTaskStartDateTime(workflowInstId, startDateTimeIsoStr)
	#boolean	stopWorkflowInstance(String workflowInstId)
	def stopWorkflowInstance(self, workflowInstId):
		return self._server.workflowmgr.stopWorkflowInstance(workflowInstId)
	#boolean	updateMetadataForWorkflow(String workflowInstId, Hashtable metadata)
	def updateMetadataForWorkflow(self, workflowInstId, metadata):
		return self._server.workflowmgr.updateMetadataForWorkflow(workflowInstId, metadata)
	#boolean	updateWorkflowInstance(Hashtable workflowInst)
	def updateWorkflowInstance(workflowInst):
		return self._server.workflowmgr.updateWorkflowInstance(workflowInst)
	#boolean	updateWorkflowInstanceStatus(String workflowInstanceId, String status)
	def updateWorkflowInstanceStatus(self, workflowInstId, status):
		return self._server.workflowmgr.updateWorkflowInstanceStatus(workflowInstId, status) 



def main():
	# create instance
    oodt = OODTWorkFlowWrapper("http://localhost:9200")
    # get event info
    events = oodt.getEventNames()
    # create metadata object to invoke an event
    met = MetaData()
    met.addMetaData("hello", "world")
    # print available events
    print 'available events:', events

    # oodt.startEvent(events[0], met.toXmlRpc())

if __name__ == '__main__':
    main()
