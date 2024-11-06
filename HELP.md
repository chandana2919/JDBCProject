JDBCUI TASK:

Overview:

This project is a database management system that allows users to connect to multiple databases, execute queries, and retrieve results in csv formats. It is designed to simplify database interactions for users who need to manage from different databases at a time.


1)Configure Database Connection :
 In the resource directory text file name called  db_info.text
 The correct order for the database information is:
<database_name><host_name><username><password>`

Ex: 

1`Dct_ap_kakinada`jdbc:mysql://192.168.1.228:3306/dct_ap_krishna`test`Test@123
7. Run the Application : deploy the application in the any of the tomcat server.
User Interface (UI)
This section will guide users on how to navigate and utilize the application's user interface after launching the project.
1. Accessing the Application
Once the application is running, you can access the user interface through your web browser. Open your preferred browser and navigate to:
  Ex :http://192.168.1.238:8099/jdbcUITask


2. Main Dashboard
Upon accessing the application, you will be presented with the main dashboard, which includes the following components:
    • Database Dropdown: A dropdown menu that allows you to select from the available databases.
    • Query Input Area: A text area where you can input SQL queries. Please ensure that you provide a complete query without syntax errors and do not include the database name in the text area. For example: SELECT * FROM users;.
    • Download and Update: A button to download the resulting data in CSV format, and an update button to refresh the data based on the query.

