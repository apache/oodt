import { wmconnection } from "constants/connection";

export const updateWorkflowStatus = (workflowInstanceId, state) => {
  return new Promise((resolve, reject) => {
    wmconnection
      .post(
        "/workflow/updatestatus?workflowInstanceId=" +
          workflowInstanceId +
          "&status=" +
          state
      )
      .then((result) => {
        resolve(result);
      })
      .catch((error) => {
        reject(error);
      });
  });
};

export const getWorkflowList = (pageNo) => {
  return new Promise((resolve, reject) => {
    wmconnection
      .get("/workflow/page",{
        params: {
          workflowPage: pageNo
        }
      })
      .then((result) => {
        resolve(result.data.workflowPageInstance);
      })
      .catch((error) => {
        reject(error);
      });
  });
};

export const getRegisteredEvents = () => {
  return new Promise((resolve, reject) => {
    wmconnection
      .get("/workflow/events")
      .then((result) => {
        resolve(result.data.workflowEvents.events);
      })
      .catch((error) => {
        reject(error);
      });
  });
}

export const handleEvent = (eventName) => {
  return new Promise((resolve, reject) => {
    wmconnection
      .post("/workflow/event",null,{
        params: {
          eventName: eventName
        }
      })
      .then((result) => {
        resolve(result);
      })
      .catch((error) => {
        reject(error);
      });
  });
}