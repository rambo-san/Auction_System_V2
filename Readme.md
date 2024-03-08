# Auction System V2.0

## Description
This project is an updated version of the Auction System. It provides a platform for users to participate in auctions and bid on items.

## Installation
1. Clone the repository: `git clone https://github.com/your-username/auction-system.git`
2. Install dependencies: `npm install`


## Database Queries

### Create a new auction
```sql

CREATE DATABASE AuctionDB;
Create the Tables:
Use the following SQL commands to create necessary tables within AuctionDB:
USE AuctionDB;

CREATE TABLE Items (
id INT AUTO_INCREMENT PRIMARY KEY,
name VARCHAR(255) NOT NULL,
description TEXT,
startTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
endTime TIMESTAMP
);

CREATE TABLE Bids (
id INT AUTO_INCREMENT PRIMARY KEY,
itemId INT,
username VARCHAR(255) NOT NULL,
bidAmount INT NOT NULL,
bidTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (itemId) REFERENCES Items(id)
);

INSERT INTO Items (name, description, endTime) VALUES ('Item Name', 'Item Description', '2024-12-31 23:59:59');

Inserting a Bid

INSERT INTO Bids (itemId, username, bidAmount) VALUES (1, 'JohnDoe', 100);

Retrieving Winning Bid

SELECT username, MAX(bidAmount) as maxBid FROM Bids WHERE itemId = 1;

Listing Auction Items

SELECT * FROM Items;

Listing Bids for an Item

SELECT * FROM Bids WHERE itemId = 1 ORDER BY bidAmount DESC;