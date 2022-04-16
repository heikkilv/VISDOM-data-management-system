"""A tool for converting project data from SQLite to MongoDB."""

# Copyright 2022 Tampere University
# This software was developed as a part of the VISDOM project: https://iteavisdom.org/
# This source code is licensed under the MIT license. See LICENSE in the repository root directory.
# Author(s): Ville Heikkilä <ville.heikkila@tuni.fi>

from dataclasses import dataclass
from datetime import datetime, timezone
from os import environ
from sqlite3 import connect as sqlite3_connect
from typing import Any, Dict, List, Optional, Union

from pymongo.mongo_client import MongoClient
from pymongo.collection import Collection as MongoCollection


TABLE_NAMES = [
    "PROJECTS",
    "GIT_COMMITS",
    "GIT_COMMITS_CHANGES",
    "JIRA_ISSUES",
    "SONAR_ANALYSIS",
    "SONAR_MEASURES"
]
# tables for which all considered columns are listed in the ATTRIBUTE_TYPES constant
STRICT_TABLES = [
    "SONAR_MEASURES"
]


@dataclass
class ValueType:
    """Dataclass to hold type information for an attribute."""
    value_type: Any = str
    optional: bool = False


ATTRIBUTE_TYPES = {
    "GIT_COMMITS": {
        "AUTHOR_DATE": ValueType(datetime, False),
        "AUTHOR_TIMEZONE": ValueType(int, False),
        "COMMITTER_DATE": ValueType(datetime, False),
        "COMMITTER_TIMEZONE": ValueType(int, False),
        "BRANCHES": ValueType(list, False),
        "IN_MAIN_BRANCH": ValueType(bool, False),
        "MERGE": ValueType(bool, False),
        "PARENTS": ValueType(list, False)
    },
    "GIT_COMMITS_CHANGES": {
        "DATE": ValueType(datetime, False),
        "LINES_ADDED": ValueType(int, False),
        "LINES_REMOVED": ValueType(int, False)
    },
    "JIRA_ISSUES": {
        "ASSIGNEE": ValueType(str, True),
        "RESOLUTION": ValueType(str, True),
        "LABELS": ValueType(list, False),
        "TIME_ORIGINAL_ESTIMATE": ValueType(float, True),
        "AGGREGATE_TIME_ORGINAL_ESTIMATE": ValueType(float, True),
        "AGGREGATE_TIME_SPENT": ValueType(float, True),
        "TIME_SPENT": ValueType(float, True),
        "TIME_ESTIMATE": ValueType(float, True),
        "AGGREGATE_TIME_ESTIMATE": ValueType(float, True),
        "CREATION_DATE": ValueType(datetime, False),
        "RESOLUTION_DATE": ValueType(datetime, True),
        "UPDATE_DATE": ValueType(datetime, False),
        "DUE_DATE": ValueType(datetime, True),
        "WATCH_COUNT": ValueType(int, False),
        "VOTES": ValueType(int, False),
        "CREATOR_ACTIVE": ValueType(bool, False),
        "VERSIONS": ValueType(list, False),
        "FIX_VERSIONS": ValueType(list, False),
        "PROGRESS_PERCENT": ValueType(float, True),
        "HASH": ValueType(str, True),
        "COMMIT_DATE": ValueType(datetime, True),
    },
    "SONAR_ANALYSIS": {
        "DATE": ValueType(datetime, False)
    },
    "SONAR_MEASURES": {
        "PROJECT_ID": ValueType(str, False),
        "ANALYSIS_KEY": ValueType(str, False),
        "COMPLEXITY": ValueType(int, True),
        "FUNCTION_COMPLEXITY": ValueType(float, True),
        "LINES_TO_COVER": ValueType(int, True),
        "DUPLICATED_LINES": ValueType(int, False),
        "DUPLICATED_BLOCKS": ValueType(int, False),
        "DUPLICATED_FILES": ValueType(int, False),
        "VIOLATIONS": ValueType(int, False),
        "BLOCKER_VIOLATIONS": ValueType(int, False),
        "CRITICAL_VIOLATIONS": ValueType(int, False),
        "MAJOR_VIOLATIONS": ValueType(int, False),
        "MINOR_VIOLATIONS": ValueType(int, False),
        "INFO_VIOLATIONS": ValueType(int, False),
        "OPEN_ISSUES": ValueType(int, False),
        "SQALE_INDEX": ValueType(int, False),
        "SQALE_DEBT_RATIO": ValueType(float, False),
        "CODE_SMELLS": ValueType(int, False),
        "ALERT_STATUS": ValueType(str, False),
        "BUGS": ValueType(int, False),
        "RELIABILITY_RATING": ValueType(int, False),
        "LAST_COMMIT_DATE": ValueType(datetime, True),
        "VULNERABILITIES": ValueType(int, False),
        "SECURITY_RATING": ValueType(int, False),
        "LINES": ValueType(int, True),
        "CLASSES": ValueType(int, True),
        "FILES": ValueType(int, True),
        "DIRECTORIES": ValueType(int, True),
        "STATEMENTS": ValueType(int, True),
        "COMMENT_LINES": ValueType(int, True)
    }
}


def get_value(value_type: ValueType, value: Any) -> Optional[Union[str, int, float, datetime, List[str]]]:
    """Returns the given value converted to the given value type."""
    if value_type.optional and (value == "" or value is None):
        return None

    if value_type.value_type is int:
        return int(value)
    if value_type.value_type is float:
        return float(value)
    if value_type.value_type is bool:
        return value in ("True", "true")

    if value_type.value_type is datetime:
        # fix the different kinds of formats to represent timezones
        cleaned_string = value.replace("Z", "+00:00") \
            .replace(" +0000", "+00:00").replace(" +0100", "+01:00").replace(" +0200", "+02:00") \
            .replace(" +0300", "+03:00").replace(" +0400", "+04:00").replace(" +0500", "+05:00") \
            .replace(" +0600", "+06:00").replace(" +0700", "+07:00").replace(" +0800", "+08:00") \
            .replace(" +0900", "+09:00").replace(" +1000", "+10:00").replace(" +1100", "+11:00") \
            .replace(" -0000", "-00:00").replace(" -0100", "-01:00").replace(" -0200", "-02:00") \
            .replace(" -0300", "-03:00").replace(" -0400", "-04:00").replace(" -0500", "-05:00") \
            .replace(" -0600", "-06:00").replace(" -0700", "-07:00").replace(" -0800", "-08:00") \
            .replace(" -0900", "-09:00").replace(" -1000", "-10:00").replace(" -1100", "-11:00") \
            .replace(" +1200", "+12:00").replace(" +1300", "+13:00").replace(" +0530", "+05:30")

        if cleaned_string:
            # add timezone information if it is missing
            if "+" not in cleaned_string and cleaned_string.count("-") != 3:
                cleaned_string += "+00:00"
            return datetime.fromisoformat(cleaned_string).astimezone(timezone.utc).isoformat()

        return None

    if value_type.value_type is list:
        if value is None:
            return []

        cleaned_string = value[1:-1].replace("'", "").replace('"', "")
        if cleaned_string:
            return [item.strip() for item in cleaned_string.split(",")]
        return []

    return str(value)


def get_document(
    attributes: List[str],
    attribute_types: Dict[str, ValueType],
    strict: bool,
    row: tuple
) -> Optional[Dict[str, Any]]:
    """Returns the given row converted to a dictionary. If the conversion cannot be done, return None."""
    try:
        return {
            key.lower(): get_value(attribute_types.get(key, ValueType()), value)
            for key, value in zip(attributes, row)
            if not strict or key in attribute_types
        }
    except (AttributeError, ValueError, TypeError) as error:
        print(type(error).__name__, error, sep=": ")
        print("with row:", row)
        return None


def write_document(collection: MongoCollection, document: Optional[Dict[str, Any]]) -> bool:
    """Tries to add the given document to the given MongoDB collection."""
    if document is None:
        return False

    write_result = collection.insert_one(document)
    return write_result.acknowledged


class SQLiteToMongoDB:
    """Class for reading data from SQLite database and writing the data to MongoDB."""

    METADATA = "metadata"

    def __init__(self) -> None:
        sqlite_connection = sqlite3_connect(environ.get("SQLITE_FILENAME", "td_V2.db"))
        self.cursor__ = sqlite_connection.cursor()

        mongo_client = MongoClient(
            host=environ.get("MONGODB_HOST", "localhost"),
            port=int(environ.get("MONGODB_PORT", 27017)),
            username=environ.get("MONGODB_USERNAME", ""),
            password=environ.get("MONGODB_PASSWORD", ""),
            appname="SQLite to MongoDB converter",
            authSource=environ.get("MONGODB_METADATA_DATABASE", "metadata")
        )
        self.mongo_database__ = mongo_client.get_database(environ.get("MONGODB_TARGET_DATABASE", "dataset"))

    def get_table_attributes(self, table_name: str) -> List[str]:
        """Returns the attribute names for the given table."""
        table_schema = self.cursor__.execute(f"PRAGMA table_info({table_name})").fetchall()
        return [
            attribute[1]
            for attribute in table_schema
        ]

    def convert_table(self, table_name: str) -> int:
        """
        Reads the data from the given SQLite table and writes the data to a Mongo collection.
        Returns the number of documents added to MongoDB.
        """
        attributes = self.get_table_attributes(table_name)
        table_is_strict = table_name in STRICT_TABLES
        collection = self.mongo_database__[table_name.lower()]

        # drop the collection from MongoDB so that there will not be duplicate documents
        collection.drop()

        counter = 0

        table_cursor = self.cursor__.execute(f"SELECT * FROM {table_name}")
        for row in table_cursor:
            document = get_document(
                attributes=attributes,
                attribute_types=ATTRIBUTE_TYPES.get(table_name, {}),
                strict=table_is_strict,
                row=row
            )
            if write_document(collection, document):
                counter += 1
                if counter % 10000 == 0:
                    print("-", counter, "documents written to MongoDB")

        return counter

    def write_metadata_document(self, table_name: str, document_count: int) -> None:
        """Writes a metadata document to MongoDB to mimic the behavior of VISDOM data management system fetcher."""
        metadata_collection = self.mongo_database__[self.METADATA]
        metadata_document = {
            "type": table_name.lower(),
            "documents_updated_count": document_count,
            "timestamp": datetime.utcnow()
        }

        if not write_document(metadata_collection, metadata_document):
            print("Problem writing a metadata document for table:", table_name)

    def convert_data(self) -> None:
        """Reads the data from all supported SQLite tables and writes the data to Mongo collections."""
        for table_name in TABLE_NAMES:
            print("Converting table:", table_name)
            written_documents = self.convert_table(table_name)
            print(f"Wrote {written_documents} documents to collection {table_name.lower()}.")
            self.write_metadata_document(table_name, written_documents)


if __name__ == "__main__":
    converter = SQLiteToMongoDB()
    converter.convert_data()
