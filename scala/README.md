# Scala source code for the Data Management System

The source code is divided into separate subprojects:

- [Core](core) contains common code that the other subprojects can utilize
- [GitLab fetcher](gitlab-fetcher) contains code for the data fetching from GitLab
- [A+ fetcher](aplus-fetcher) contains code for the data fetching from A+
- [Adapter core](core-adapter) contains code that multiple data adapters are using
- [GitLab adapter](gitlab-adapter) contains code for a data adapter capable of handling GitLab data
- [Course adapter](adapter-course) contains code for a data adapter capable of handling programming course data from A+ and GitLab
- [General data model adapter](adapter-general-model) contains code for a data adapter giving the the data in a general software data model and supporting raw data from GitLab and A+ data
- [Dataset adapter](adapter-dataset) contains code for a data adapter capable of handling raw data from a specific software project dataset
- [Data broker](broker) contains code for a simple data broker

The installation instructions and the component descriptions can be found at the [documentation](../documentation) folder.
