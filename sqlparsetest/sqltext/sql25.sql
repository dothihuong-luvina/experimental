SELECT plate.programname, class,
    COUNT(specObjId) AS numObjs
    FROM SpecObjAll 
    JOIN PlateX AS plate ON plate.plate = specObjAll.plate
    GROUP BY plate.programname, class
    ORDER BY plate.programname, class

