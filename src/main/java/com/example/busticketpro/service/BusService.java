package com.example.busticketpro.service;

import com.example.busticketpro.dto.BusDTO;
import com.example.busticketpro.model.Bus;
import com.example.busticketpro.repository.BusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;
import java.util.List;

@Service
public class BusService {

    @Autowired
    private BusRepository busRepo;

    public Bus createBus(BusDTO dto) {
        if (busRepo.findByLicensePlate(dto.getLicensePlate()).isPresent()) {
            throw new RuntimeException("Biển số xe đã tồn tại");
        }
        Bus bus = new Bus();
        bus.setLicensePlate(dto.getLicensePlate());
        bus.setType(dto.getType());
        bus.setSeatCount(dto.getSeatCount());
        bus.setDriverName(dto.getDriverName());
        return busRepo.save(bus);
    }
    public List<Bus> getAll() {
        return busRepo.findAll();
    }

    public Bus updateBus(Long id, @Valid BusDTO dto) {
        Bus bus = busRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy xe với id: " + id));
        bus.setLicensePlate(dto.getLicensePlate());
        bus.setType(dto.getType());
        bus.setSeatCount(dto.getSeatCount());
        bus.setDriverName(dto.getDriverName());
        return busRepo.save(bus);
    }

    public void deleteBus(Long id) {
        if (!busRepo.existsById(id)) {
            throw new RuntimeException("Không tìm thấy xe để xóa với id: " + id);
        }
        busRepo.deleteById(id);
    }

}
