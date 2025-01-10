// Pre-request Script for QI FdR KPI "Calculate KPI - Date Too Recent" test
// To be pasted into the Scripts -> Pre-request body, either on the collection or on the specific test call mentioned above
const today = new Date();
const year = today.getFullYear();
const month = String(today.getMonth() + 1).padStart(2, '0');
const day = String(today.getDate()).padStart(2, '0');

const formattedDate = `${year}-${month}-${day}`;

pm.environment.set('DATE_TOO_RECENT', formattedDate);