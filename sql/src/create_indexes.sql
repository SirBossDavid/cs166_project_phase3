/*
--AirLine Mangment

--gets the flight schedule, given the flight number
SELECT *
FROM Schedule
WHERE FlightNumber = '<:flight_number>'
ORDER BY 
    CASE 
      WHEN DayOfWeek = 'Monday' THEN 1
      WHEN DayOfWeek = 'Tuesday' THEN 2
      WHEN DayOfWeek = 'Wednesday' THEN 3
      WHEN DayOfWeek = 'Thursday' THEN 4
      WHEN DayOfWeek = 'Friday' THEN 5
      WHEN DayOfWeek = 'Saturday' THEN 6
      WHEN DayOfWeek = 'Sunday' THEN 7
      ELSE 8
    END;

--gets number of seats availabe and sold
SELECT SeatsTotal - SeatsSold as SeatsAvailable, SeatsSold
FROM FlightInstance
WHERE FlightNumber = '<:flight_number>'
  AND FlightDate = '<:flight_date>';

--find wheater the flight departed and arrived on time
SELECT DepartedOnTime, ArrivedOnTime
FROM FlightInstance
WHERE FlightNumber = '<:flight_number>'
  AND FlightDate = '<:flight_date>';

--get all flights scheduled on that day
SELECT *
FROM FlightInstance fi
JOIN Flight f on fi.FlightNumber = f.FlightNumber
WHERE fi.FlightDate = '<:flight_date>'
ORDER BY fi.DepartureTime;

--list of who made reservations, are on the waiting list, and who acutally flew on the flight
SELECT C.FirstName, C.LastName, R.Status
FROM Reservation R
JOIN Customer C 
  ON R.CustomerID = C.CustomerID
JOIN FlightInstance FI
  ON R.FlightInstanceID = FI.FlightInstanceID
WHERE 
    FI.FlightNumber = '<:flight_number>'
    AND FI.FlightDate = '<:flight_date>'
ORDER BY 
  CASE
    WHEN R.Status = 'reserved' THEN 1
    WHEN R.Status = 'waitlist' THEN 2
    WHEN R.Status = 'flown'    THEN 3
    ELSE 4
  END,
  C.LastName,
  C.FirstName;

--given res id, get the info of travelers under that number
SELECT C.CustomerID, C.FirstName, C.LastName,C.Gender,
       C.DOB, C.Address, C.Phone, C.Zip
FROM Reservation R
JOIN Customer C
  ON R.CustomerID = C.CustomerID
WHERE 
    R.ReservationID = '<:reservation_id>';

--given plane id, get plane info
-- Input: :plane_id  (e.g. 'PLANE-A123')

SELECT P.Make,P.Model,
    EXTRACT(YEAR FROM AGE(CURRENT_DATE, TO_DATE(P.Year::TEXT, 'YYYY'))) AS Age,
    (SELECT MAX(Rp.RepairDate)
     FROM Repair Rp
     WHERE Rp.PlaneID = P.PlaneID
    ) AS LastRepairDate
FROM Plane P
WHERE P.PlaneID = '<:plane_id>';

--given tech id, get all their repairs
SELECT 
    R.RepairID,
    R.PlaneID,
    R.RepairCode,
    R.RepairDate
FROM Repair R
WHERE 
    R.TechnicianID = '<:technician_id>'
ORDER BY R.RepairDate DESC;

--given plane id and date range, list all dates and codes of repairs
SELECT 
    R.RepairID,
    R.RepairCode,
    R.RepairDate,
    R.TechnicianID
FROM Repair R
WHERE 
    R.PlaneID = '<:plane_id>'
    AND R.RepairDate BETWEEN '<:start_date>' AND '<:end_date>'
ORDER BY R.RepairDate;

--given a flight number and date range, show stats of that flight
SELECT
  COUNT(*) FILTER (WHERE FI.DepartedOnTime = TRUE)  AS NumDepartedOnTime,
  COUNT(*) FILTER (WHERE FI.DepartedOnTime = FALSE) AS NumDepartedDelayed,
  COUNT(*) FILTER (WHERE FI.ArrivedOnTime = TRUE)   AS NumArrivedOnTime,
  COUNT(*) FILTER (WHERE FI.ArrivedOnTime = FALSE)  AS NumArrivedDelayed,
  SUM(COALESCE(FI.SeatsSold,0))                     AS TotalSeatsSold,
  SUM(COALESCE(FI.SeatsTotal,0) - COALESCE(FI.SeatsSold,0)) AS TotalSeatsUnsold
FROM FlightInstance FI
WHERE 
    FI.FlightNumber = '<:flight_number>'
    AND FI.FlightDate BETWEEN '<:start_date>' AND '<:end_date>';  */