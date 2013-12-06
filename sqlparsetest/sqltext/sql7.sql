SELECT TOP 100
    sp.objID, 
    sp.ra, 
    sp.dec, 
    sp.mjd, 
    px.taiBegin, 
    px.taiEnd, 
    sp.fiberID, 
    sp.z 
FROM specPhoto AS sp
JOIN plateX AS px
    ON sp.plateID = px.plateID 
WHERE
    (sp.class='QSO') 
    AND sp.z > 3 

