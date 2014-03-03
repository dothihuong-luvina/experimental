require 'socket'
loop{
begin
sleep(3)
s = TCPSocket.new 'localhost', 2809
s.puts "[Socket] : This is a log message from socket !"
rescue Exception => ex
puts ex
end
}
